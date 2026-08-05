package ui11;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.WidgetDefinitionParser.InjectionFieldInfo;
import ui11.WidgetDefinitionParser.StateFieldInfo;
import ui11.WidgetState.IVCollector;
import ui11.WidgetState.InheritedPropBase;
import ui11.observable.MutableObservable;
import ui11.reflectutil.ReflectionUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodType.methodType;

// ha ezt módosítjuk, módosítsuk TeaVMWidgetAccessor-ban is
final class RegularWidgetAccessor<T extends Widget> implements WidgetAccessor<T> {

    private static final Logger logger = LoggerFactory.getLogger(RegularWidgetAccessor.class);

    private final AnnotatedElement elementDefinition; // exception message-ekhez. bár jelenleg nincs használva
    private final Class<T> clazz;
    private final List<Field> inputFields;
    private final List<InjectionFieldInfo> injectFields;
    private final List<StateFieldInfo> stateFields;
    private final List<ProviderMethodDecorator<T, ?>> decorators;

    private final Class<?>[] ivTypes;

    // detachedmarkeres kavarás
    private final RegularWidgetAccessor<T> other;
    private final boolean isDetachedMarker;

    @SuppressWarnings("unchecked")
    public RegularWidgetAccessor(WidgetDefinitionParser reflector) {
        this.elementDefinition = reflector.clazz;

        List<ProviderMethodDecorator<T, ?>> decorators2 = new ArrayList<>();
        for (WidgetDefinitionParser.ProviderMethodInfo<?> providerMethodInfo : reflector.providers) {
            decorators2.add(new ProviderMethodDecorator<>(providerMethodInfo));
        }
        this.decorators = decorators2;

        clazz = (Class<T>) reflector.clazz;
        // előre vesszük a final fieldeket, hogy ne maradjon egy state role-ú widget olyan állapotban,
        // hogy az input mezők értékeinek egy részét már lecserélték, a maradékot viszont skippelték mert final mező
        // változott
        inputFields = reflector.inputFields;
        injectFields = reflector.injectFields;
        stateFields = reflector.stateFields;

        this.ivTypes = reflector.ivCollectorTypes;

        this.isDetachedMarker = false;
        this.other = new RegularWidgetAccessor<>(this);
    }

    /**
     * detached markert hoz létre, csak a másik konstruktorból van hívva
     */
    private RegularWidgetAccessor(RegularWidgetAccessor<T> other) {
        this.elementDefinition = other.elementDefinition;
        this.clazz = other.clazz;
        this.inputFields = other.inputFields;
        this.injectFields = other.injectFields;
        this.stateFields = other.stateFields;
        this.decorators = other.decorators;
        this.ivTypes = other.ivTypes;
        this.isDetachedMarker = true;
        this.other = other;
    }

    @Override
    public Class<T> clazz() {
        return clazz;
    }

    @Override
    public boolean prepareListenerProxies(T modelWidget) {
        boolean haveListenerProxies = false;
        for (int i = 0; i < inputFields.size(); i++) {
            Field f = inputFields.get(i);
            Object value = fieldGet(modelWidget, f);
            if (value instanceof ListenerProxyBase<?> proxy)
                haveListenerProxies |= proxy.init(modelWidget, i);
        }
        return haveListenerProxies;
    }

    @Override
    public void checkStateEmptyAndPrepareState(T newState, WidgetState<T> widgetState, T model) {
        for (StateFieldInfo f : stateFields) {
            Object value = fieldGet(newState, f.field());
            if (!Objects.equals(f.zeroValue(), value))
                throw new RuntimeException("The value of field " + ReflectionUtil.memberToShortString(f.field()) +
                        " was modified before " + clazz.getSimpleName() + ".initState() of " + newState + "\n" +
                        "Refresh stack: \n" + newState.debug_getRefreshStack());

            if (f.isObservable())
                fieldSet(newState, f.field(), MutableObservable.ofNullable(f.zeroValueOfObservable()));
        }
        // lehetne úgy is hogy egy külön ciklusban megyünk végig az observable-kön, és
        // akkor nem fordul elő hogy néhány state fieldnek már értékül adtunk MutableObservable-ket amikor
        // exceptiont dobtunk, de úgy a TeaVM-es implementációt bonyolítaná.
        // esetleg lehet csinálni rollbacket (tehát kinullázzuk a már beírt observable-ket).

        if (widgetState.ivCollectors != null)
            throw new IllegalStateException();
        IVCollector<?>[] ivCollectors = new IVCollector[ivTypes.length];
        for (int i = 0; i < ivCollectors.length; i++)
            ivCollectors[i] = new IVCollector<>(widgetState, ivTypes[i]);
        widgetState.ivCollectors = ivCollectors;

        for (InjectionFieldInfo f : injectFields) {
            // primitív típus nem lehet egy @Inject mező típusa
            if (fieldGet(newState, f.field()) != null)
                throw new RuntimeException("The value of field " + ReflectionUtil.memberToShortString(f.field()) +
                        " was modified instead of leaving it as null in " + newState + "\n" +
                        "Refresh stack: \n" + newState.debug_getRefreshStack());

            Object wrapper = switch (f.kind()) {
                case NORMAL -> null;
                case INTERFACE_PROXY -> {
                    MethodHandle mh = INTERFACE_PROXY_FACTORY_CV.get(f.type());
                    try {
                        yield mh.invokeExact(widgetState, f.type(), f.optional());
                    } catch (RuntimeException | Error e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
                case OBSERVABLE -> {
                    IVCollector<?> collector = ivCollectors[f.collectorIndex()];
                    assert collector.type == f.type();
                    yield new WidgetState.InheritedPropObservable<>(
                            collector, f.optional(), f.debugName());
                }
            };

            fieldSet(newState, f.field(), wrapper);
        }
    }

    /**
     * @param oldModel az, amelyre a {@link ListenerProxyBase#isOwnedBy(Widget)} {@code oldState}-en meghívva true-t ad
     *                 vissza
     */
    @Override
    public InputFieldChangeDetectionResult areInputFieldsChanged(T oldModel, T newModel) {
        for (Field f : inputFields) {
            Object a = fieldGet(oldModel, f);
            Object b = fieldGet(newModel, f);
            if (a instanceof ListenerProxyBase<?> aProxy && aProxy.isOwnedBy(oldModel) &&
                    b instanceof ListenerProxyBase<?> bProxy && bProxy.isOwnedBy(newModel)) {
                continue;
            }

            if (!Objects.equals(a, b))
                return InputFieldChangeDetectionResult.NEEDS_UPDATE;
        }

        for (Field f : inputFields) {
            Object a = fieldGet(oldModel, f);
            Object b = fieldGet(newModel, f);
            if (a instanceof ListenerProxyBase<?> aProxy && aProxy.isOwnedBy(oldModel) &&
                    b instanceof ListenerProxyBase<?> bProxy && bProxy.isOwnedBy(newModel)) {

                if (aProxy.hasSameValue(bProxy))
                    // nem fogjuk változtatni az értékét, ezért nem baj ha tovább használjuk aProxy-t
                    continue;

                return InputFieldChangeDetectionResult.NEEDS_LISTENER_PROXY_BACKPROPAGATION;
            }
        }

        return InputFieldChangeDetectionResult.NOT_NEEDS_UPDATE;
    }

    @Override
    public void transferState(T fromState, T toState) {
        for (StateFieldInfo f : stateFields) {
            Object aValue = fieldGet(fromState, f.field());
            // TODO nem tudjuk ellenőrizni hogy volt-e módosítva, mert lehet hogy már
            //      MutableObservable-t létrehozott a checkStateEmptyAndPrepareState ha az a field típusa
            /*
            Object bValue = fieldGet(toState, f.field());
            if (!Objects.equals(f.zeroValue(), bValue))
                // TODO exception message jobb TeaVMWidgetAccessorban
                throw new RuntimeException("The value of field " + ReflectionUtil.memberToShortString(f.field()) +
                        " has been tampered in " + bValue + "\n" +
                        "Refresh stack: \n" + toState.debug_getRefreshStack());
             */
            fieldSet(toState, f.field(), aValue);
        }

        // TODO inject fieldeknél lehetne ellenőrizni, hogy nem lettek-e átállítva
        for (InjectionFieldInfo f : injectFields) {
            Object aValue = fieldGet(fromState, f.field());
            Object bValue = fieldGet(toState, f.field());
            if (!Objects.equals(null, bValue))
                throw new RuntimeException("The value of field " + ReflectionUtil.memberToShortString(f.field()) +
                        " has been tampered in " + bValue + "\n" +
                        "Refresh stack: \n" + toState.debug_getRefreshStack());
            fieldSet(toState, f.field(), aValue);
        }
    }

    @Override
    public void copyIVValuesToFields(T t) {
        for (int i = 0; i < injectFields.size(); i++) {
            InjectionFieldInfo f = injectFields.get(i);
            switch (f.kind()) {
                case NORMAL -> {
                    Object value = t.widgetState().ivCollectors[f.collectorIndex()].
                            currentValue(f.optional(), f.debugName());
                    fieldSet(t, f.field(), value);
                }
                case OBSERVABLE, INTERFACE_PROXY -> {
                    Object fieldValue = fieldGet(t, f.field());
                    InheritedPropBase<?> inheritedProp = (InheritedPropBase<?>) fieldValue;
                    inheritedProp.update();
                }
                default -> {
                    throw new RuntimeException("unknown injection field kind: " + f);
                }
            }
        }
    }

    @Override
    public Object readNonPrimitiveInputField(T t, int inputField) {
        return fieldGet(t, inputFields.get(inputField));
    }

    @Override
    public boolean inputFieldsEquals(T a, T b) {
        for (Field f : inputFields) {
            Object aValue = fieldGet(a, f), bValue = fieldGet(b, f);
            if (!Objects.equals(aValue, bValue))
                return false;
        }
        return true;
    }

    @Override
    public int inputFieldsHashCode(T w) {
        int h = hashCode();
        for (Field f : inputFields) {
            Object fieldValue = fieldGet(w, f);
            h = h * 23 + Objects.hashCode(fieldValue);
        }
        return h;
    }

    @Override
    public Object[] inputFieldsToString(T w) {
        Object[] a = new Object[inputFields.size() * 2];
        int i = 0;
        for (Field f : inputFields) {
            a[i++] = f.getName();
            a[i++] = fieldGet(w, f);
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
            // nem lehetséges, mert WidgetDefinitionParser setAccessible(true)-t hívott meg minden fieldre
            throw new RuntimeException(e);
        }
    }

    private void fieldSet(T widget, Field f, Object value) {
        try {
            f.set(widget, value);
        } catch (IllegalAccessException e) {
            // nem lehetséges, mert WidgetDefinitionParser setAccessible(true)-t hívott meg minden fieldre
            throw new RuntimeException(e);
        }
    }

    @Override
    public Widget decorate(T w, @NonNull Widget content) {
        for (ProviderMethodDecorator<T, ?> decorator : decorators)
            content = decorator.decorate(w, content);
        return content;
    }

    @Override
    public WidgetAccessor<T> asDetachedMarker(boolean detached) {
        if (detached == this.isDetachedMarker)
            return this;
        else
            return other;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RegularWidgetAccessor<?> a && asDetachedMarker(false) == a.asDetachedMarker(false);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(asDetachedMarker(false));
    }

    @Override
    public String toString() {
        return super.toString() + " " + clazz.getName() + " detached=" + isDetachedMarker;
    }

    private static final ClassValue<MethodHandle> INTERFACE_PROXY_FACTORY_CV = new ClassValue<MethodHandle>() {
        @Override
        protected MethodHandle computeValue(Class<?> interfaceType) {
            InheritedInterfaceProxyGenerator g = new InheritedInterfaceProxyGenerator(interfaceType);
            byte[] classfile = g.toClassfile();

            // TODO privateLookupIn-nek target classnak nem az interface-t, hanem a widget osztáláyt kéne
            //      megadnunk, mert a interface moduljához nem biztos hogy van private accessünk
            Lookup proxyClassLookup;
            try {
                proxyClassLookup = MethodHandles.privateLookupIn(interfaceType, lookup()).
                        defineHiddenClass(classfile, true);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("can't define proxy class for interface " + interfaceType.getName() + ": " + e, e);
            }
            MethodHandle mh; // (LWidgetState;LClass;Z)LinterfaceType;
            try {
                // TODO hidden class miatt valszeg nem működik a DirectMethodHandleDesc
                mh = g.factoryMethod().resolveConstantDesc(proxyClassLookup);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("can't access factory method of proxy class for " +
                        "interface " + interfaceType.getName() + ": " + e, e);
            }
            return mh.asType(methodType(Object.class, WidgetState.class, Class.class, boolean.class, String.class));
        }
    };
}
