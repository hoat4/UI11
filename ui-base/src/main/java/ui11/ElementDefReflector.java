package ui11;

import org.teavm.metaprogramming.Metaprogramming;
import org.teavm.metaprogramming.Value;
import ui11.ElementDefReflector.DecoratorMetadata.Priority;
import ui11.Widget.Inject;
import ui11.Widget.Listener;
import ui11.meta.DecoratorAnnotation;
import ui11.meta.DecoratorAnnotation.DecoratorAnnotationHandler;
import ui11.meta.DecoratorAnnotation.DecoratorAnnotationHandler.Decorator;
import ui11.meta.DecoratorAnnotation.TeaVMSupportingDecoratorAnnotationHandler;
import ui11.observable.Observable;
import ui11.provide.Provide;
import ui11.reflectutil.ReflectionUtil;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Comparator.comparing;

@org.teavm.metaprogramming.CompileTime
class ElementDefReflector {

    // MetaprogrammingClassLoader miatt Element.class-t nem lehet használni ebből az osztályból
    static final Class<Element> ELEMENT_CLASS;

    static {
        ClassLoader metaprogrammingClassLoader;
        boolean isTeaVM;
        try {
            metaprogrammingClassLoader = Metaprogramming.getClassLoader();
            isTeaVM = true;
        } catch (UnsupportedOperationException uoe) {
            metaprogrammingClassLoader = null;
            isTeaVM = false;
        }

        // TODO
        ClassLoader cl;

        if (isTeaVM) {
            cl = metaprogrammingClassLoader.getParent();
        } else {
            cl = ElementDefReflector.class.getClassLoader();
        }

        try {
            @SuppressWarnings("unchecked") final Class<Element> c = (Class<Element>) cl.loadClass(
                    "ui11.Element");
            ELEMENT_CLASS = c;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public final Class<? extends Widget> clazz;
    private final boolean isTeaVM;

    List<DecoratorMetadata> decorators;

    List<InputFieldInfo> inputFields;
    List<Field> stateFields;
    List<InjectionFieldInfo> injectFields;

    public ElementDefReflector(Class<? extends Widget> clazz, boolean isTeaVM) {
        if (!Widget.class.isAssignableFrom(clazz) || clazz == Widget.class)
            throw new RuntimeException("not a Widget subtype: " + clazz.getName());

        this.isTeaVM = isTeaVM;
        Objects.requireNonNull(clazz);
        this.clazz = clazz;
    }

    public void reflect() {
        decorators = new ArrayList<>();

        for (Class<?> c2 : ReflectionUtil.ancestorsAndThis(clazz))
            for (Annotation ann : c2.getDeclaredAnnotations())
                processClassAnnotation(ann);

        processReactiveStatefulWidgetFields(clazz);

        // TODO RSW-nél @Provide-nak kéne supportálni nem Observable mezőket is
        for (Field f : ReflectionUtil.fieldsIn(clazz)) {// superclasstól subclass felé halad a mezőkkel
            Provide ann = f.getAnnotation(Provide.class);
            if (ann != null)
                parseProviderTagAnnotation(ann, f);
        }

        for (Method m : ReflectionUtil.methodsIn(clazz)) {// TODO sorrend decorationannotationöknél?
            Provide ann = m.getAnnotation(Provide.class);
            if (ann != null)
                parseProviderTagAnnotation(ann, m);
        }

        decorators.sort(comparing(d -> d.priority().ordinal()));
    }

    private void processClassAnnotation(Annotation ann) {
        DecoratorAnnotation metaAnn2 = ann.annotationType().getAnnotation(DecoratorAnnotation.class);
        if (metaAnn2 != null)
            parseDecoratorAnnotation(ann, metaAnn2);
    }

    private <ANN extends Annotation> void parseDecoratorAnnotation(ANN ann, DecoratorAnnotation metaAnn) {
        @SuppressWarnings("unchecked")  // TODO
        Class<? extends DecoratorAnnotationHandler<ANN>> handler =
                (Class<? extends DecoratorAnnotationHandler<ANN>>) metaAnn.handler();

        decorators.add(new DecoratorMetadata(new CustomDecoratorReflector<>(handler, ann), Priority.INNER));
    }

    private void parseProviderTagAnnotation(Provide ann, AnnotatedElement member) {
        ProviderMethodReflector<?> r = new ProviderMethodReflector<>(member, ann);
        decorators.add(new DecoratorMetadata(r, Priority.OUTEST));
    }

    @SuppressWarnings("ConstantValue")
    private void processReactiveStatefulWidgetFields(Class<?> edClass) {
        this.inputFields = new ArrayList<>();
        this.injectFields = new ArrayList<>();
        this.stateFields = new ArrayList<>();

        for (Field f : ReflectionUtil.fieldsIn(edClass)) {
            if (f.getDeclaringClass() == Widget.class)
                continue;

            boolean isInject = f.isAnnotationPresent(Widget.Inject.class);
            boolean isState = f.isAnnotationPresent(Widget.State.class);
            int sum = (isInject ? 1 : 0) + (isState ? 1 : 0);

            boolean isInterfaceProxy = f.isAnnotationPresent(Listener.class);

            if (Modifier.isStatic(f.getModifiers()))
                if (sum == 0 && !isInterfaceProxy)
                    continue;
                else {
                    throw new RuntimeException("only non-static fields of a " + Widget.class.getSimpleName() +
                            " can be annotated with " +
                            "@" + Widget.Inject.class.getSimpleName() + " or " +
                            "@" + Widget.State.class.getSimpleName() + " or " +
                            "@" + Widget.Listener.class.getSimpleName() + ", but " +
                            ReflectionUtil.memberToShortString(f) + " is static");
                }

            f.setAccessible(true);

            boolean isInput = sum == 0;

            if (sum > 1) {
                throw new RuntimeException("every non-static field in a " + Widget.class.getSimpleName() +
                        " must be annotated with at most one of " +
                        "@" + Widget.Inject.class.getSimpleName() + " or " +
                        "@" + Widget.State.class.getSimpleName() + ", but " +
                        ReflectionUtil.memberToShortString(f) + " is annotated with more than one of these");
            }


            if (isInput) {
                if (!Modifier.isFinal(f.getModifiers())) {
                    throw new RuntimeException("a non-static field in a " + Widget.class.getSimpleName() + " " +
                            "not annotated with @" + Inject.class.getSimpleName() + " or " +
                            "@" + Widget.State.class.getSimpleName() + " must be final, but " +
                            ReflectionUtil.memberToShortString(f) + " is not final");
                }

                // eredetileg úgy volt, hogy tetszőleges interface-ek lehetnek event listenerek.
                // de lehet hogy jobb így, hogy csak Runnable meg egy-két másik lehet, mert így biztosítani lehet,
                // hogy csak void retu
                // rn type-ú SAM-ok.
                // 2025-12-06:
                // majd lehet hogy ki kell terjeszteni tetszőleges interfacere (pl. mouseeventek esetén a tipikus a
                // sok függvényes interface, vagy lehet hogy kell visszaadni értéket), de egyelőre elég ez a kettő.

                if (isInterfaceProxy && f.getType() != Runnable.class && f.getType() != Consumer.class)
                    throw new RuntimeException("a field annotated with @" + Widget.Listener.class.getSimpleName() +
                            " must have type of java.lang.Runnable or java.util.function.Consumer, but " +
                            ReflectionUtil.memberToShortString(f) + " has type " + f.getType().getName());

                inputFields.add(new InputFieldInfo(f, isInterfaceProxy));
            } else if (isInject) {
                if (Modifier.isFinal(f.getModifiers())) {
                    throw new RuntimeException("a field annotated with " +
                            "@" + Widget.Inject.class.getSimpleName() +
                            " must not be final, but " +
                            ReflectionUtil.memberToShortString(f) + " is final");
                }
                if (isInterfaceProxy)
                    throw new RuntimeException("a field annotated with " +
                            "@" + Widget.Inject.class.getSimpleName() +
                            " must not be annotated with @" + Widget.Listener.class.getSimpleName() +
                            ", but " + ReflectionUtil.memberToShortString(f) + " annotated with it");

                Class<?> ivType;
                boolean interfaceProxy;
                if (f.getType() == Observable.class) {
                    if (f.getGenericType() instanceof ParameterizedType parameterizedType)
                        ivType = ReflectionUtil.rawType(parameterizedType.getActualTypeArguments()[0]);
                    else {
                        // TODO "actual inherited value type" helyett mit lehetne írni? kb. mint "bean type"
                        throw new RuntimeException("If the type of a field annotated with " +
                                "@" + Widget.Inject.class.getSimpleName() + " is " +
                                Observable.class.getName() + ", then it must be parameterized with the actual inherited " +
                                "value type, but " + ReflectionUtil.memberToShortString(f) + " has type " +
                                f.getGenericType().getTypeName() + " which is not a parameterized " +
                                Observable.class.getSimpleName() + "<...> type");
                    }
                    interfaceProxy = false;
                } else if (f.getType() == Slot.class || f.getType() == MultiSlot.class) {
                    ivType = f.getType();
                    interfaceProxy = false;
                } else {
                    if (!f.getType().isInterface())
                        throw new RuntimeException("The type of a field annotated with " +
                                "@" + Widget.Inject.class.getSimpleName() + " must be an interface or " +
                                Observable.class.getSimpleName() +
                                ", but " + ReflectionUtil.memberToShortString(f) + " has type " + f.getType().getName());

                    ivType = f.getType();
                    interfaceProxy = true;
                }

                boolean optional = !f.getAnnotation(Inject.class).required();
                if (optional && interfaceProxy)
                    throw new RuntimeException("'required' must be true if @" + Inject.class.getSimpleName() + " used" +
                            " on a non-Observable field: " + ReflectionUtil.memberToShortString(f));

                String debugFieldName = ReflectionUtil.memberToShortString2(f);
                injectFields.add(new InjectionFieldInfo(f, ivType, optional, interfaceProxy, debugFieldName));
            } else {
                assert isState;

                if (Modifier.isFinal(f.getModifiers())) {
                    throw new RuntimeException("a field annotated with " +
                            "@" + Widget.State.class.getSimpleName() +
                            " must not be final, but " +
                            ReflectionUtil.memberToShortString(f) + " is final");
                }
                if (isInterfaceProxy)
                    throw new RuntimeException("a field annotated with " +
                            "@" + Widget.State.class.getSimpleName() +
                            " must not be annotated with @" + Widget.Listener.class.getSimpleName() +
                            ", but " + ReflectionUtil.memberToShortString(f) + " annotated with it");

                stateFields.add(f);
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.memberToShortString(clazz);
    }

    @Nonnull
    static <T> T invokeDefaultConstructor(Class<T> handlerClass)
            throws ReflectiveOperationException {
        Constructor<T> c = handlerClass.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    record DecoratorMetadata(DecoratorReflector decoratorReflector, DecoratorMetadata.Priority priority) {

        enum Priority {
            INNER, EVENT, OUTER, OUTEST
        }
    }

    sealed interface DecoratorReflector permits ProviderMethodReflector, CustomDecoratorReflector {

        Decorator<?> makeDecorator();

        Value<? extends Widget> makeTeaVMCode(Value<? extends Widget> o, Value<?> container, int decoratorIndex);

        default boolean neededForAllChildren() {
            return false;
        }
    }

    final class CustomDecoratorReflector<ANN extends Annotation> implements DecoratorReflector {

        public final Class<? extends DecoratorAnnotationHandler<ANN>> handlerClass;
        public final ANN annotation;

        public CustomDecoratorReflector(Class<? extends DecoratorAnnotationHandler<ANN>> handlerClass, ANN annotation) {
            this.handlerClass = handlerClass;
            this.annotation = annotation;
        }

        @Override
        public Decorator<?> makeDecorator() {
            DecoratorAnnotationHandler<ANN> handler;
            try {
                handler = ElementDefReflector.invokeDefaultConstructor(handlerClass);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Couldn't create " + handlerClass.getName() +
                        " for " + ReflectionUtil.memberToShortString(clazz) + ": " + e, e);
            }
            return handler.makeDecorator(clazz, annotation);
        }

        @Override
        public Value<? extends Widget> makeTeaVMCode(Value<? extends Widget> o, Value<?> container, int decoratorIndex) {
            @SuppressWarnings("unchecked")
            Class<? extends TeaVMSupportingDecoratorAnnotationHandler<ANN>> handlerClass2 =
                    (Class<? extends TeaVMSupportingDecoratorAnnotationHandler<ANN>>)
                            TeaVMElementAccessorFactory.getTeaVMSupportClassOfHandler(handlerClass).
                                    asSubclass(TeaVMSupportingDecoratorAnnotationHandler.class);
            TeaVMSupportingDecoratorAnnotationHandler<ANN> handler;
            try {
                handler = ElementDefReflector.invokeDefaultConstructor(handlerClass2);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Couldn't create " + handlerClass2.getName() +
                        " for " + ReflectionUtil.memberToShortString(clazz) + ": " + e, e);
            }
            Value<? extends Widget> o2 = handler.generateDecoratorCode(clazz, annotation, container, o);
            String handlerName = handlerClass2.toString();
            return Metaprogramming.emit(() -> {
                if (o2.get() == null)
                    throw new NullPointerException(handlerName + " returned null for " + container.get());
                return o2.get();
            });
        }
    }

    static {
        // TODO teavm MetaprogrammingClassLoader hack
        try {
            Class.forName("ps.common.ui.FontIconAnnotationHandler");
        } catch (ClassNotFoundException e) {
        }
    }

    record InputFieldInfo(Field field, boolean interfaceProxy) {
    }

    record InjectionFieldInfo(Field field, Class<?> type, boolean optional, boolean interfaceProxy, String debugName) {

        public boolean isNotInherited() {
            return type == Slot.class || type == MultiSlot.class;
        }

        // identity equals, mert key-ként van használva, ahol elég az identity equals (és nyilván gyorsabb)

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public String toString() {
            // Slot.baseIdentifier miatt kell
            return ReflectionUtil.memberToShortString2(field);
        }
    }
}
