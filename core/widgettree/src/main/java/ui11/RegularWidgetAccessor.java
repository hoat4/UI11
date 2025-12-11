package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.ElementDefReflector.DecoratorMetadata;
import ui11.ElementDefReflector.InjectionFieldInfo;
import ui11.ElementDefReflector.InputFieldInfo;
import ui11.meta.DecoratorAnnotation.DecoratorAnnotationHandler.Decorator;
import ui11.observable.Observable;
import ui11.reflectutil.ReflectionUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.lang.invoke.MethodType.methodType;

// ha ezt módosítjuk, módosítsuk TeaVMElementAccessorFactory-ban is
class RegularWidgetAccessor<T extends Widget> implements WidgetAccessor<T> {

    private static final Logger logger = LoggerFactory.getLogger(RegularWidgetAccessor.class);

    private static final MethodHandle NULL_FROM_Object_Element = MethodHandles.dropArguments(
            MethodHandles.zero(Object.class), 0, Object.class,
            Element.class);
    private static final MethodHandle Objects_isNull;

    static {
        try {
            Objects_isNull = MethodHandles.lookup().findStatic(Objects.class, "isNull",
                    methodType(boolean.class, Object.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("should not happen", e);
        }
    }

    protected final AnnotatedElement elementDefinition; // exception message-ekhez. bár jelenleg nincs használva
    private final Class<T> clazz;
    private final List<InputFieldInfo> inputFields;
    private final List<InjectionFieldInfo> injectFields;
    private final List<Field> stateFields;
    private final List<Decorator<T>> delegateDecorators;
    private final List<Decorator<T>> allChildDecorators;

    @SuppressWarnings("unchecked")
    public RegularWidgetAccessor(ElementDefReflector reflector) {
        this.elementDefinition = reflector.clazz;

        List<Decorator<T>> delegateDecorators1 = new ArrayList<>();
        List<Decorator<T>> allChildDecorators1 = new ArrayList<>();
        for (DecoratorMetadata decoratorMetadata : reflector.decorators) {
            Decorator<T> decorator = (Decorator<T>) decoratorMetadata.decoratorReflector().makeDecorator();
            delegateDecorators1.add(decorator);
            if (decoratorMetadata.decoratorReflector().neededForAllChildren())
                allChildDecorators1.add(decorator);
        }
        this.delegateDecorators = delegateDecorators1;
        this.allChildDecorators = allChildDecorators1;

        clazz = (Class<T>) reflector.clazz;
        inputFields = reflector.inputFields;
        injectFields = reflector.injectFields;
        stateFields = reflector.stateFields;
    }

    /**
     * (Ljava/lang/Object;LElement;)Ljava/lang/Object;
     */
    private static MethodHandle makeListenerProxy(RecordComponent recordComponent) {
        MethodHandle mh = MethodHandles.dropArguments(ListenerProxyGenerator.makeProxyFactory(recordComponent),
                0, Object.class);
        return MethodHandles.guardWithTest(Objects_isNull, NULL_FROM_Object_Element, mh);
    }

    @Override
    public Class<T> clazz() {
        return clazz;
    }

    @Override
    public void initAndCopyState(@Nullable T oldWidget, @Nonnull T newWidget) {
        for (Field f : stateFields) {
            Object newValue = fieldGet(newWidget, f);
            if (!Objects.equals(ReflectionUtil.defaultValue(f.getType()), newValue))
                throw new RuntimeException("The value of field " + ReflectionUtil.memberToShortString(f) +
                        " was modified before " + clazz.getSimpleName() + ".init() of " + newWidget + "\n" +
                        "Refresh stack: \n" + newWidget.debug_getRefreshStack());

            if (oldWidget != null) {
                Object oldValue = fieldGet(oldWidget, f);
                fieldSet(newWidget, f, oldValue);
            }
        }

        // lehetne ellenőrizni itt is, hogy volt-e módosítva a field értéke
        if (!newWidget.injectFieldsInitialized) {
            for (int i = 0; i < injectFields.size(); i++) {
                int ivIndex = i;
                InjectionFieldInfo f = injectFields.get(i);
                Object wrapper;
                if (f.interfaceProxy())
                    wrapper = Proxy.newProxyInstance(f.type().getClassLoader() /* ??? */, new Class[]{f.type()},
                            (proxy, m, args) -> {
                                // TODO Object.equals?
                                Object value = newWidget.getInheritedValueByIndex(ivIndex);
                                return m.invoke(value, args);
                            });
                else if (f.type() == Slot.class)
                    wrapper = new Slot(newWidget, f); // InjectionFieldInfo kommentje szerint lehet key-nek használni
                else if (f.type() == MultiSlot.class)
                    wrapper = new MultiSlot<>(new Slot(newWidget, f)); // InjectionFieldInfo kommentje szerint lehet key-nek használni
                else
                    wrapper = Observable.of(() -> newWidget.getInheritedValueByIndex(ivIndex));
                fieldSet(newWidget, f.field(), wrapper);
            }
            newWidget.injectFieldsInitialized = true;
        }

        for (InputFieldInfo f : inputFields) {
            if (!f.interfaceProxy())
                continue;

            Object newValue = fieldGet(newWidget, f.field());
            if (newValue == null)
                continue;

            Object oldValue = oldWidget == null ? null : fieldGet(oldWidget, f.field());
            if (oldValue != null) {
                ListenerProxyBase2<?> l = (ListenerProxyBase2<?>) oldValue;
                l.repurposeForNewWidget(oldWidget, newWidget, newValue);
                newValue = l;
            } else {
                if (f.field().getType() == Runnable.class)
                    newValue = new RunnableProxy(newWidget, (Runnable) newValue);
                else if (f.field().getType() == Consumer.class)
                    newValue = new ConsumerProxy<>(newWidget, (Consumer<?>) newValue);
                else
                    throw new RuntimeException("unknown listener type on " +
                            ReflectionUtil.memberToShortString(f.field()));
            }
            // többször is felülírhatjuk a mezőt (ha különböző RSWStateHolderek között rángatják a Widgetet)
            fieldSet(newWidget, f.field(), newValue);
        }
    }

    @Override
    public Observable<?>[] observeInheritedValues(RSWStateHolder<T> stateHolder) {
        Observable<?>[] ivObsList = new Observable[injectFields.size()];
        for (int i = 0; i < injectFields.size(); i++) {
            InjectionFieldInfo f = injectFields.get(i);
            if (f.isNotInherited())
                continue;
            ivObsList[i] = stateHolder.inherited(f.type(), f.optional());
        }
        return ivObsList;
    }

    @Override
    public void checkStateEmpty(T w) {
        for (Field f : stateFields) {
            Object value = fieldGet(w, f);
            if (!Objects.equals(ReflectionUtil.defaultValue(f.getType()), value))
                throw new RuntimeException("The value of field " + ReflectionUtil.memberToShortString(f) +
                        " was modified before " + clazz.getSimpleName() + ".init() of " + w);
        }
    }

    @Override
    public boolean inputFieldsEquals(T a, T b) {
        for (InputFieldInfo f : inputFields) {
            Object aValue = fieldGet(a, f.field()), bValue = fieldGet(b, f.field());
            if (!Objects.equals(aValue, bValue))
                return false;
        }
        return true;
    }

    @Override
    public boolean inputFieldsEqualsAndTransferListeners(T a, T b) {
        for (InputFieldInfo f : inputFields) {
            Object aValue = fieldGet(a, f.field()), bValue = fieldGet(b, f.field());
            if (f.interfaceProxy()) {
                if ((aValue == null) != (bValue == null))
                    return false;
            } else {
                if (!Objects.equals(aValue, bValue))
                    return false;
            }
        }

        for (InputFieldInfo f : inputFields) {
            if (!f.interfaceProxy())
                continue;
            Object aValue = fieldGet(a, f.field()), bValue = fieldGet(b, f.field());
            if (aValue == null)
                continue;
            ((ListenerProxyBase2<?>) aValue).propagateChangeToOldWidget(a, b, bValue);
        }
        return true;
    }

    @Override
    public int inputFieldsHashCode(T w) {
        int h = hashCode();
        for (InputFieldInfo f : inputFields) {
            Object fieldValue = fieldGet(w, f.field());
            h = h * 23 + Objects.hashCode(fieldValue);
        }
        return h;
    }

    @Override
    public Object[] inputFieldsToString(T t) {
        Object[] a = new Object[inputFields.size() * 2];
        int i = 0;
        for (InputFieldInfo f : inputFields) {
            a[i++] = f.field().getName();
            a[i++] = fieldGet(t, f.field());
        }
        return a;
    }

    private static boolean valueEquals(Object a, Object b, Class<?> type) {
        if (type.isPrimitive())
            // boxolva vannak, ezért nem jó az ==. majd ha lesz Valhalla, akkor jó lesz az is.
            return a.equals(b); // null nem lehet egyik se, mert primitív típusok
        else
            return a == b;
    }

    private Object fieldGet(T widget, Field f) {
        try {
            return f.get(widget);
        } catch (IllegalAccessException e) {
            // nem lehetséges, mert ElementDefReflector setAccessible(true)-t hívott meg minden fieldre
            throw new RuntimeException(e);
        }
    }

    private void fieldSet(T widget, Field f, Object value) {
        try {
            f.set(widget, value);
        } catch (IllegalAccessException e) {
            // nem lehetséges, mert ElementDefReflector setAccessible(true)-t hívott meg minden fieldre
            throw new RuntimeException(e);
        }
    }

    @Override
    public Widget decorate(T e, @Nonnull Widget content, boolean isDelegate) {
        for (Decorator<T> decorator : isDelegate ? delegateDecorators : allChildDecorators) {
            if (decorator.applies(e)) {
                content = decorator.decorate(e, content);
            }
        }
        return content;
    }
}
