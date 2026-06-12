package ui11;

import ui11.Widget.Inject;
import ui11.Widget.Remember;
import ui11.WidgetDefinitionParser.InjectionFieldInfo.InjectedFieldKind;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.Provide;
import ui11.reflectutil.ReflectionUtil;
import ui11.reflectutil.Types;
import ui11.resolution.SubstitutedWidget;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@org.teavm.metaprogramming.CompileTime
class WidgetDefinitionParser {

    public final Class<? extends Widget> clazz;

    List<Field> inputFields;
    List<StateFieldInfo> stateFields;
    List<InjectionFieldInfo> injectFields;

    List<ProviderMethodInfo<?>> providers;

    public WidgetDefinitionParser(Class<? extends Widget> clazz) {
        if (!Widget.class.isAssignableFrom(clazz) || clazz == Widget.class) // TODO lehet hogy abstractot is kéne nézni
            throw new RuntimeException("not a Widget subtype: " + clazz.getName());

        Objects.requireNonNull(clazz);
        this.clazz = clazz;
    }

    public static boolean isWidgetType(Class<?> c) {
        return Widget.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers());
    }

    public void reflect() {
        processReactiveStatefulWidgetFields(clazz);
        processProviderFieldsAndMethods();
    }

    @SuppressWarnings("ConstantValue")
    private void processReactiveStatefulWidgetFields(Class<?> edClass) {
        this.inputFields = new ArrayList<>();
        this.injectFields = new ArrayList<>();
        this.stateFields = new ArrayList<>();

        for (Field f : ReflectionUtil.fieldsIn(edClass)) {
            if (f.getDeclaringClass() == Widget.class)
                continue;

            boolean isInput;
            boolean isInject = f.isAnnotationPresent(Inject.class);
            boolean isState = f.isAnnotationPresent(Remember.class);

            if (Modifier.isStatic(f.getModifiers()))
                if (isInject || isState)
                    throw new InvalidWidgetDefinitionException("@" + Inject.class.getSimpleName() + " and " +
                            "@" + Remember.class.getSimpleName() + " cannot be used on static fields: " +
                            ReflectionUtil.memberToShortString(f));
                else
                    continue;

            // transientek lehetnek a mezők?

            boolean isSubstitutedWidget = SubstitutedWidget.class.isAssignableFrom(edClass);
            if ((isInject || isState) && isSubstitutedWidget && f.getDeclaringClass() != SubstitutedWidget.class)
                throw new InvalidWidgetDefinitionException("@" + Inject.class.getSimpleName() + " and @" +
                        Remember.class.getSimpleName() + " " +
                        "cannot be used on fields in a " + SubstitutedWidget.class.getSimpleName() + " subtype: " +
                        ReflectionUtil.memberToShortString(edClass));

            if ((isInject || isState) && EndingWidget.class.isAssignableFrom(edClass))
                throw new InvalidWidgetDefinitionException("@" + Inject.class.getSimpleName() + " and @" +
                        Remember.class.getSimpleName() + " " +
                        "cannot be used on fields in a " + EndingWidget.class.getSimpleName() + " subtype: " +
                        ReflectionUtil.memberToShortString(edClass));

            // TODO ezekben az exception messageekben nincs szó slotokról, pedig azok is lehetnek @Inject-esek

            if (isInject && isState)
                throw new InvalidWidgetDefinitionException("""
                        Field FIELD is annotated with both @INJECT_ANN and @STATE_ANN. \
                        A field may be annotated with at most one of these annotations.
                        How to choose:
                        - @INJECT_ANN: The value is provided by an ancestor widget and \
                        should be injected by the framework.
                        - @STATE_ANN: The value is owned by this widget and should persist \
                        across rebuilds and input changes.
                        - neither annotation but 'final': Value comes from the parent widget \
                        (for example through a constructor parameter).""".
                        replace("FIELD", ReflectionUtil.memberToShortString(f)).
                        replace("INJECT_ANN", Inject.class.getSimpleName()).
                        replace("STATE_ANN", Remember.class.getSimpleName()));

            isInput = !isInject && !isState;

            f.setAccessible(true);

            if (isInput) {
                if (!Modifier.isFinal(f.getModifiers()))
                    throw new InvalidWidgetDefinitionException("""
                            Field FIELD is non-final but has neither @INJECT_ANN nor @STATE_ANN.
                            Every field in a WIDGET_TYPE must declare how its value is managed:
                            - final field: if the value is an input property, so its value is \
                            set by the parent widget (for example through a constructor parameter)
                            - @INJECT_ANN if value is provided by an ancestor widget and \
                            should be injected by the framework.
                            - @STATE_ANN if the value is owned by this widget and should persist \
                            across rebuilds and input changes.""".
                            replace("FIELD", ReflectionUtil.memberToShortString(f)).
                            replace("WIDGET_TYPE", Widget.class.getSimpleName()).
                            replace("INJECT_ANN", Inject.class.getSimpleName()).
                            replace("STATE_ANN", Remember.class.getSimpleName()));

                inputFields.add(f);
            } else if (isInject) {
                if (Modifier.isFinal(f.getModifiers()))
                    throw new InvalidWidgetDefinitionException("""
                            Field FIELD is declared final and annotated with @INJECT_ANN.
                            Fields annotated with @INJECT_ANN must not be final, \
                            because the framework couldn't inject their values.
                            Choose one:
                            - Keep the field final if it is an input property instead of an injectable field.
                            - Remove the final modifier if it should be injected.""".
                            replace("FIELD", ReflectionUtil.memberToShortString(f)).
                            replace("INJECT_ANN", Inject.class.getSimpleName()));

                Class<?> ivType;
                InjectedFieldKind kind;
                if (f.getType() == Observable.class) {
                    if (f.getGenericType() instanceof ParameterizedType parameterizedType)
                        ivType = ReflectionUtil.rawType(parameterizedType.getActualTypeArguments()[0]);
                    else {
                        // TODO "actual inherited value type" helyett mit lehetne írni? kb. mint "bean type"
                        throw new InvalidWidgetDefinitionException("If the type of a field annotated with " +
                                "@" + Widget.Inject.class.getSimpleName() + " is " +
                                Observable.class.getName() + ", then it must be parameterized with the actual inherited " +
                                "value type, but " + ReflectionUtil.memberToShortString(f) + " has type " +
                                f.getGenericType().getTypeName() + " which is not a parameterized " +
                                Observable.class.getSimpleName() + "<...> type");
                    }
                    kind = InjectedFieldKind.OBSERVABLE;

                    // TODO ez most nincs rendesen megcsinálva, mert akkor is folyton invalidálunk, ha nem is használjuk
                    //      az értékét
                } else if (f.getType() == Slot.class || f.getType() == MultiSlot.class) {
                    ivType = f.getType();
                    kind = InjectedFieldKind.SLOT_OR_MULTI_SLOT;
                } else {
                    if (f.getType().isPrimitive())
                        throw new InvalidWidgetDefinitionException(
                                ReflectionUtil.memberToShortString(f) + " has type " + f.getType().getName() +
                                        " which is a primitive, but the type of a field annotated with " +
                                        "@" + Widget.Inject.class.getSimpleName() + " must be an class or interface (" +
                                        "including specially treated types " + Slot.class.getSimpleName() + ", " +
                                        MultiSlot.class.getSimpleName() + ", " + Observable.class.getSimpleName() +
                                        "), not a primitive type");

                    ivType = f.getType();
                    kind = InjectedFieldKind.NORMAL;

                    /*
                    Felmerült, hogy lehessen expliciten kérni interface proxy-t.
                    De ehhez el kéne dönteni, hogy mit csináljon.
                    Először arra gondoltam, hogy cacheelje a return value-kat, és megváltozásuk esetén invalidáljon,
                    de ez két okból sem jó:
                    - nem egyértelmű hogy mennyi argumentum-kombinációt kéne cacheelni, ha túl sok van
                    - side-effectek, pl. Scheduler
                    Scheduler ráadásul külön bonyolult helyzet, mert ott ha megváltozik, akkor az újba is fel
                    kéne tölteni a taskokat.

                    Talán olyasmi lehetne, hogy az első meghíváskor subscribeol az érték megváltozására.
                    Így lehetne arra használni, hogy ha nem biztos hogy használjuk, akkor ne invalidáljunk feleslegesen.

                    if (!f.getType().isInterface())
                        throw new InvalidWidgetDefinitionException("The type of a field annotated with " +
                                "@" + Widget.Inject.class.getSimpleName() + " must be an interface or " +
                                Observable.class.getSimpleName() +
                                ", but " + ReflectionUtil.memberToShortString(f) + " has type " + f.getType().getName());

                    ivType = f.getType();
                    kind = InjectedFieldKind.INTERFACE_PROXY;
                    */
                }

                boolean optional = !f.getAnnotation(Inject.class).required();
                if (optional && kind == InjectedFieldKind.INTERFACE_PROXY)
                    throw new InvalidWidgetDefinitionException("'required' must be true if @" + Inject.class.getSimpleName() + " used" +
                            " on an interface proxy field: " + ReflectionUtil.memberToShortString(f));

                String debugFieldName = ReflectionUtil.memberToShortString2(f);
                injectFields.add(new InjectionFieldInfo(f, ivType, kind, optional, debugFieldName));
            } else {
                assert isState;

                if (Modifier.isFinal(f.getModifiers()))
                    throw new InvalidWidgetDefinitionException("""
                            Field FIELD is declared final and annotated with @STATE_ANN.
                            Fields annotated with @STATE_ANN must not be final, \
                            because the framework couldn't copy their values from a previous widget instance.
                            Choose one:
                            - Keep the field final if it is an input property instead of an remembered field.
                            - Remove the final modifier if it should be remembered between input property updates.""".
                            replace("FIELD", ReflectionUtil.memberToShortString(f)).
                            replace("STATE_ANN", Inject.class.getSimpleName()));

                boolean isObservable = f.getType() == MutableObservable.class;
                Object zeroValueOfObservable = null;
                if (isObservable) {
                    if (!(f.getAnnotatedType() instanceof AnnotatedParameterizedType parameterizedType)) {
                        throw new InvalidWidgetDefinitionException("a field annotated with @" + Remember.class.getSimpleName() +
                                " and type " + MutableObservable.class.getSimpleName() + " must have a type argument");
                    }
                    Class<?> paramRawType =
                            ReflectionUtil.rawType(parameterizedType.getAnnotatedActualTypeArguments()[0]);
                    zeroValueOfObservable =
                            ReflectionUtil.defaultValue(Types.replaceWrapperWithPrimitive(paramRawType));
                }
                Object zeroValue = ReflectionUtil.defaultValue(f.getType());

                stateFields.add(new StateFieldInfo(f, zeroValue, isObservable, zeroValueOfObservable));
            }
        }
    }

    private void processProviderFieldsAndMethods() {
        providers = new ArrayList<>();
        // TODO RSW-nél @Provide-nak kéne supportálni nem Observable mezőket is
        for (Field f : ReflectionUtil.fieldsIn(clazz)) {// superclasstól subclass felé halad a mezőkkel
            Provide ann = f.getAnnotation(Provide.class);
            if (ann != null) {
                if (f.getType() != MutableObservable.class && f.getType() != Observable.class)
                    throw new InvalidWidgetDefinitionException("a @" + Provide.class.getSimpleName() + " field " +
                            "must have type " + MutableObservable.class.getSimpleName() + "<...> or " +
                            Observable.class.getSimpleName() + "<...>, " +
                            "but " + ReflectionUtil.memberToShortString(f) + " has type: " +
                            ReflectionUtil.typeToString(f.getAnnotatedType()));
                if (Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers()))
                    throw new InvalidWidgetDefinitionException("a field which is annotated with " + ann.annotationType() + " " +
                            "must be final and non static");
                // TODO jelezni kéne értelmes üzenettel, ha rawtype
                Class<?> type =
                        ReflectionUtil.rawType(((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]);
                providers.add(new ProviderMethodInfo(f, type));
            }
        }

        for (Method m : ReflectionUtil.methodsIn(clazz)) {// TODO sorrend decorationannotationöknél?
            Provide ann = m.getAnnotation(Provide.class);
            if (ann != null) {
                if (Modifier.isStatic(m.getModifiers()))
                    throw new InvalidWidgetDefinitionException("a static method " +
                            ReflectionUtil.memberToShortString(m) +
                            " is annotated with @" + Provide.class.getSimpleName() + ", " +
                            "but only non-static methods can be annotated with it");
                if (m.getReturnType() == void.class)
                    // TODO ki kéne írni hogy miért nem lehet
                    throw new InvalidWidgetDefinitionException("return type of " +
                            ReflectionUtil.memberToShortString(m) + " is void, but annotated with " +
                            "@" + Provide.class.getSimpleName());
                if (m.getParameterCount() != 0)
                    throw new InvalidWidgetDefinitionException(
                            ReflectionUtil.memberToShortString(m) + " has parameters, but annotated with " +
                                    "@" + Provide.class.getSimpleName());
                providers.add(new ProviderMethodInfo(m, m.getReturnType()));
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.memberToShortString(clazz);
    }

    record InjectionFieldInfo(Field field, Class<?> type, InjectedFieldKind kind, boolean optional, String debugName) {

        @Override
        public String toString() {
            // Slot.baseIdentifier miatt kell
            return ReflectionUtil.memberToShortString2(field);
        }

        public enum InjectedFieldKind {
            OBSERVABLE,
            NORMAL,
            INTERFACE_PROXY,
            SLOT_OR_MULTI_SLOT
        }
    }

    record StateFieldInfo(Field field, Object zeroValue, boolean isObservable, Object zeroValueOfObservable) {
    }

    record ProviderMethodInfo<V>(AnnotatedElement member, Class<V> providedType) {}
}
