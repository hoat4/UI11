package ui11;

import org.teavm.ast.InvocationExpr;
import org.teavm.ast.InvocationType;
import org.teavm.backend.javascript.TeaVMJavaScriptHost;
import org.teavm.backend.javascript.codegen.SourceWriter;
import org.teavm.backend.javascript.spi.*;
import org.teavm.dependency.*;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.core.JSArray;
import org.teavm.jso.core.JSString;
import org.teavm.jso.impl.JS;
import org.teavm.jso.impl.JSWrapper;
import org.teavm.model.FieldReference;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodReference;
import org.teavm.model.ValueType;
import org.teavm.platform.PlatformClass;
import org.teavm.vm.spi.TeaVMHost;
import org.teavm.vm.spi.TeaVMPlugin;
import ui11.WidgetDefinitionParser.InjectionFieldInfo;
import ui11.WidgetDefinitionParser.ProviderMethodInfo;
import ui11.WidgetDefinitionParser.StateFieldInfo;
import ui11.WidgetState.InheritedProp;
import ui11.WidgetState.InheritedPropBase;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.reflectutil.ReflectionUtil;

import org.jspecify.annotations.NonNull;

import java.lang.constant.DirectMethodHandleDesc;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

// ha ezt módosítjuk, módosítsuk RegularWidgetAccessort is.
// érdemesebb először azt módosítani és utána ezt, mert az átláthatóbb.
class TeaVMWidgetAccessor<W extends Widget> implements WidgetAccessor<W> {

    // ez a 9 mező lesz megadva makeObservableWithInitial-nek
    private static final Object ZERO_Z = false;
    private static final Object ZERO_B = (byte) 0;
    private static final Object ZERO_S = (short) 0;
    private static final Object ZERO_C = (char) 0;
    private static final Object ZERO_I = 0;
    private static final Object ZERO_F = 0f;
    private static final Object ZERO_J = 0L;
    private static final Object ZERO_D = 0D;
    private static final Object NULL_OBSERVABLE_MARKER = "ZL";

    private final Class<W> clazz;
    /**
     * sorrend: <ul>
     * <li>először final fieldek</li>
     * <li>majd interface proxy-s fieldek</li>
     * <li>végül a többi</li>
     * </ul>
     */
    private final JSArray<JSString> inputFieldNames;
    private final int finalInputFieldCount; // TODO ez nincs használva
    private final JSArray<Class<?>> interfaceProxyInputFieldTypes; // TODO ez nincs használva
    private final JSArray<JSString> injectFieldNames;
    private final JSArray<Class<?>> injectFieldTypes;
    private final JSArray<JSObject> injectFieldInterfaceProxyFactories;
    /**
     * bitmask
     */
    private final int injectFieldsOptional, injectFieldsInherited, injectFieldsObservableWrapped;
    private final JSArray<JSString> stateFieldNames;
    // most ebbe vannak belekódolva az observable-s zero-k is. lehet hogy jobb lenne annak egy külön mező.
    private final JSArray<JSObject> stateFieldZeroes;
    private final JSArray<JSString> providerFieldNames;
    private final JSArray<JSObject> providerMethods;
    /**
     * elején a mezők, utána a metódusok típusai
     */
    private final JSArray<Class<?>> providerTypes;

    // detachedmarkeres kavarás
    private final TeaVMWidgetAccessor<W> other;
    private final boolean isDetachedMarker;

    private TeaVMWidgetAccessor(Class<W> clazz,
                                JSArray<JSString> inputFieldNames,
                                int finalInputFieldCount,
                                JSArray<Class<?>> interfaceProxyInputFieldTypes,
                                JSArray<JSString> injectFieldNames,
                                JSArray<Class<?>> injectFieldTypes,
                                JSArray<JSObject> injectFieldInterfaceProxyFactories,
                                int injectFieldsOptional,
                                int injectFieldsInherited,
                                int injectFieldsObservableWrapped,
                                JSArray<JSString> stateFieldNames,
                                JSArray<JSObject> stateFieldZeroes,
                                JSArray<JSString> providerFieldNames,
                                JSArray<JSObject> providerMethods,
                                JSArray<Class<?>> providerTypes) {
        this.clazz = clazz;
        this.inputFieldNames = inputFieldNames;
        this.finalInputFieldCount = finalInputFieldCount;
        this.interfaceProxyInputFieldTypes = interfaceProxyInputFieldTypes;
        this.injectFieldNames = injectFieldNames;
        this.injectFieldTypes = injectFieldTypes;
        this.injectFieldInterfaceProxyFactories = injectFieldInterfaceProxyFactories;
        this.injectFieldsOptional = injectFieldsOptional;
        this.injectFieldsInherited = injectFieldsInherited;
        this.injectFieldsObservableWrapped = injectFieldsObservableWrapped;
        this.stateFieldNames = stateFieldNames;
        this.stateFieldZeroes = stateFieldZeroes;
        this.providerFieldNames = providerFieldNames;
        this.providerMethods = providerMethods;
        this.providerTypes = providerTypes;
        this.isDetachedMarker = false;
        this.other = new TeaVMWidgetAccessor<>(this);
    }

    /**
     * detached markert hoz létre, csak a másik konstruktorból van hívva
     */
    private TeaVMWidgetAccessor(TeaVMWidgetAccessor<W> other) {
        this.clazz = other.clazz;
        this.inputFieldNames = other.inputFieldNames;
        this.finalInputFieldCount = other.finalInputFieldCount;
        this.interfaceProxyInputFieldTypes = other.interfaceProxyInputFieldTypes;
        this.injectFieldNames = other.injectFieldNames;
        this.injectFieldTypes = other.injectFieldTypes;
        this.injectFieldInterfaceProxyFactories = other.injectFieldInterfaceProxyFactories;
        this.injectFieldsOptional = other.injectFieldsOptional;
        this.injectFieldsInherited = other.injectFieldsInherited;
        this.injectFieldsObservableWrapped = other.injectFieldsObservableWrapped;
        this.stateFieldNames = other.stateFieldNames;
        this.stateFieldZeroes = other.stateFieldZeroes;
        this.providerFieldNames = other.providerFieldNames;
        this.providerMethods = other.providerMethods;
        this.providerTypes = other.providerTypes;
        this.isDetachedMarker = true;
        this.other = other;
    }

    /**
     * ezt hívja a generált JS kód. Azért nem a konstruktort, mert azt komplikáltabb (először kell objektumot allokálni,
     * és csak utána lehet a konstruktort meghívni).
     */
    @SuppressWarnings("unchecked")
    private static <W extends Widget> void make(
            JSObject clazz,
            JSArray<JSString> inputFieldNames,
            int finalInputFieldCount,
            JSArray<Object /*PlatformClass*/> interfaceProxyInputFieldTypes,
            JSArray<JSString> injectFieldNames,
            JSArray<Object /*PlatformClass*/> injectFieldTypes,
            JSArray<JSObject /*function*/> injectFieldInterfaceProxyFactories,
            int injectFieldsOptional,
            int injectFieldsInherited,
            int injectFieldsObservableWrapped,
            JSArray<JSString> stateFieldNames,
            JSArray<JSObject> stateFieldZeroes,
            JSArray<JSString> providerFieldNames,
            JSArray<JSObject> providerMethods,
            JSArray<Object /* PlatformClass */> providerTypes) {

        for (int i = 0; i < interfaceProxyInputFieldTypes.getLength(); i++) {
            PlatformClass nativeClass = (PlatformClass) interfaceProxyInputFieldTypes.get(i);
            interfaceProxyInputFieldTypes.set(i, nativeClassToJavaClass(nativeClass));
        }
        for (int i = 0; i < injectFieldTypes.getLength(); i++) {
            PlatformClass nativeClass = (PlatformClass) injectFieldTypes.get(i);
            injectFieldTypes.set(i, nativeClassToJavaClass(nativeClass));
        }
        for (int i = 0; i < providerTypes.getLength(); i++) {
            PlatformClass nativeClass = (PlatformClass) providerTypes.get(i);
            providerTypes.set(i, nativeClassToJavaClass(nativeClass));
        }

        TeaVMWidgetAccessor<W> accessor = new TeaVMWidgetAccessor<>(
                (Class<W>) nativeClassToJavaClass(clazz),
                inputFieldNames,
                finalInputFieldCount,
                (JSArray<Class<?>>) (JSArray<?>) interfaceProxyInputFieldTypes,
                injectFieldNames,
                (JSArray<Class<?>>) (JSArray<?>) injectFieldTypes,
                injectFieldInterfaceProxyFactories,
                injectFieldsOptional,
                injectFieldsInherited,
                injectFieldsObservableWrapped,
                stateFieldNames,
                stateFieldZeroes,
                providerFieldNames,
                providerMethods,
                (JSArray<Class<?>>) (JSArray<?>) providerTypes);
        putWidgetAccessor(clazz, (JSObject) accessor);
    }

    @Override
    public Class<W> clazz() {
        return clazz;
    }

    @Override
    public boolean prepareListenerProxies(W modelWidget) {
        boolean haveListenerProxies = false;
        for (int i = 0; i < inputFieldNames.getLength(); i++) {
            JSString f = inputFieldNames.get(i);
            Object value = getInputFieldValue(modelWidget, f);
            // TODO a JSWrapper.isJava nélkül is megy az instanceof?
            if (JSWrapper.isJava(JSWrapper.dependencyJavaToJs(value)) &&
                    value instanceof ListenerProxyBase<?> proxy)
                haveListenerProxies |= proxy.init(modelWidget, i);
        }
        return haveListenerProxies;
    }

    @Override
    public void checkStateEmptyAndPrepareState(W newState, WidgetState<W> widgetState, W model) {
        for (int i = 0; i < stateFieldNames.getLength(); i++) {
            JSString stateFieldName = stateFieldNames.get(i);
            Object value = getStateFieldValue(newState, stateFieldName);

            JSObject expectedZero = stateFieldZeroes.get(i);
            if (JSWrapper.isJava(expectedZero)) {
                // observable state field

                if (value != null)
                    throw stateModifiedBeforeInit(newState, stateFieldName);

                MutableObservable<?> observable =
                        makeObservableWithInitial(JSWrapper.dependencyJsToJava(expectedZero));
                fieldSet(newState, stateFieldName, observable);
            } else {
                // non-observable state field
                if (JSWrapper.dependencyJavaToJs(value) != expectedZero)
                    throw stateModifiedBeforeInit(newState, stateFieldName);
            }
        }

        for (int i = 0; i < injectFieldNames.getLength(); i++) {
            JSString injectFieldName = injectFieldNames.get(i);

            // primitív típus nem lehet egy @Inject mező típusa
            if (getInjectFieldValue(newState, injectFieldName) != null)
                throw new RuntimeException("The value of field \"" + injectFieldName + "\"" +
                        " was modified instead of leaving it as null in " + newState + "\n" +
                        "Refresh stack: \n" + newState.debug_getRefreshStack());

            Class<?> type = injectFieldTypes.get(i);
            JSObject interfaceProxyFactory = injectFieldInterfaceProxyFactories.get(i);
            Object wrapper;
            if (interfaceProxyFactory != null)
                wrapper = invokeLLIL(interfaceProxyFactory,
                        JSWrapper.dependencyJavaToJs(widgetState),
                        JSWrapper.dependencyJavaToJs(type),
                        injectFieldsOptional >>> i & 1,
                        JSWrapper.dependencyJavaToJs(injectFieldName.stringValue()));
            else {
                if (type == Slot.class)
                    wrapper = new Slot(widgetState);
                else if (type == MultiSlot.class)
                    wrapper = new MultiSlot<>(widgetState);
                else if ((injectFieldsObservableWrapped >>> i & 1) != 0)
                    // ha ezt megváltoztatjuk, akkor lent változtassuk meg propagateType-nak átadott classnevet is
                    wrapper = new InheritedProp(widgetState, type,
                            (injectFieldsOptional & 1 << i) != 0, injectFieldName.stringValue());
                else
                    continue;
            }
            fieldSet(newState, injectFieldName, wrapper);
        }
    }

    @Override
    public InputFieldChangeDetectionResult areInputFieldsChanged(W oldModel, W newModel) {
        for (int i = 0; i < inputFieldNames.getLength(); i++) {
            JSString f = inputFieldNames.get(i);

            Object a = JSWrapper.maybeWrap(getInputFieldValue(oldModel, f));
            Object b = JSWrapper.maybeWrap(getInputFieldValue(newModel, f));
            if (a instanceof ListenerProxyBase<?> aProxy && aProxy.isOwnedBy(oldModel) &&
                    b instanceof ListenerProxyBase<?> bProxy && bProxy.isOwnedBy(newModel)) {
                continue;
            }

            if (!Objects.equals(a, b))
                return InputFieldChangeDetectionResult.NEEDS_UPDATE;
        }

        for (int i = 0; i < inputFieldNames.getLength(); i++) {
            JSString f = inputFieldNames.get(i);

            Object a = getInputFieldValue(oldModel, f);
            Object b = getInputFieldValue(newModel, f);
            if (JSWrapper.isJava(JSWrapper.dependencyJavaToJs(a)) &&
                    a instanceof ListenerProxyBase<?> aProxy && aProxy.isOwnedBy(oldModel) &&
                    JSWrapper.isJava(JSWrapper.dependencyJavaToJs(b)) &&
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
    public void transferState(W fromState, W toState) {
        for (int i = 0; i < stateFieldNames.getLength(); i++) {
            JSString fieldName = stateFieldNames.get(i);
            //JSObject expectedZero = stateFieldZeroes.get(i);

            Object aValue = getStateFieldValue(fromState, fieldName);
            /*
            Object bValue = getStateFieldValue(toState, fieldName);
            if (JSWrapper.dependencyJavaToJs(bValue) != expectedZero)
                throw stateModifiedBeforeInit(toState, fieldName);
             */
            fieldSet(toState, fieldName, aValue);
        }

        for (int i = 0; i < injectFieldNames.getLength(); i++) {
            JSString fieldName = injectFieldNames.get(i);

            Object aValue = getInjectFieldValue(fromState, fieldName);
            Object bValue = getInjectFieldValue(toState, fieldName);
            if (bValue != null)
                throw new RuntimeException("The value of @" + Widget.Inject.class.getSimpleName() + " field \"" + fieldName + "\"" +
                        " has been tampered in " + bValue + "\n" +
                        "Refresh stack: \n" + toState.debug_getRefreshStack());
            fieldSet(toState, fieldName, aValue);
        }
    }

    @Override
    public void retrieveInheritedValues(W w) {
        for (int i = 0; i < injectFieldNames.getLength(); i++) {
            if ((injectFieldsObservableWrapped & 1 << i) == 0 && injectFieldInterfaceProxyFactories.get(i) == null) {
                Class<?> type = injectFieldTypes.get(i);
                if (type != Slot.class && type != MultiSlot.class) {
                    // InjectedFieldKind.NORMAL

                    boolean optional = (injectFieldsOptional >>> i & 1) != 0;
                    JSString fieldName = injectFieldNames.get(i);
                    Object value = w.element().findInheritedValueForInjection(type, optional, fieldName.stringValue());
                    fieldSet(w, fieldName, value);
                } else {
                    // InjectedFieldKind.SLOT_OR_MULTI_SLOT
                }
            } else {
                // InjectedFieldKind.OBSERVABLE, InjectedFieldKind.INTERFACE_PROXY

                Object fieldValue = getInjectFieldValue(w, injectFieldNames.get(i));
                InheritedPropBase<?> inheritedProp = (InheritedPropBase<?>) fieldValue;
                inheritedProp.update();
            }
        }
    }

    @Override
    public Object readNonPrimitiveInputField(W w, int inputField) {
        if (inputField < 0 || inputField >= inputFieldNames.getLength())
            throw new RuntimeException("invalid input field index: " + inputField + " in " + clazz.getName());
        Object rawValue = getInputFieldValue(w, inputFieldNames.get(inputField));
        if (rawValue != null && !JSWrapper.isJava(JSWrapper.dependencyJavaToJs(rawValue)))
            throw new RuntimeException("not object value in \"" + inputFieldNames.get(inputField) +
                    "\" (" + inputField + ") of " + w);
        return rawValue;
    }

    // azért van kiemelve, hogy lehessen a paraméterhez DependencyNode-okat kötni (a fenti 9 static field)
    private static @NonNull MutableObservable<Object> makeObservableWithInitial(Object initialValue) {
        return MutableObservable.ofNullable(initialValue == NULL_OBSERVABLE_MARKER ? null : initialValue);
    }

    private @NonNull RuntimeException stateModifiedBeforeInit(W w, JSString stateFieldName) {
        return new RuntimeException("The value of @" + Widget.Remember.class.getSimpleName() + " field " +
                clazz.getSimpleName() + "." + stateFieldName +
                " was modified before " + clazz.getSimpleName() + ".init() of " + w + "\n" +
                "Refresh stack: \n" + w.debug_getRefreshStack());
    }

    @Override
    public boolean inputFieldsEquals(W a, W b) {
        for (int i = 0; i < inputFieldNames.getLength(); i++) {
            JSString fieldName = inputFieldNames.get(i);
            Object aValue = JSWrapper.maybeWrap(getInputFieldValue(a, fieldName)),
                    bValue = JSWrapper.maybeWrap(getInputFieldValue(b, fieldName));
            if (!Objects.equals(aValue, bValue))
                return false;
        }
        return true;
    }

    @Override
    public int inputFieldsHashCode(W w) {
        int h = hashCode();
        for (int i = 0; i < inputFieldNames.getLength(); i++) {
            JSString fieldName = inputFieldNames.get(i);
            Object fieldValue = JSWrapper.maybeWrap(getInputFieldValue(w, fieldName));
            h = h * 23 + Objects.hashCode(fieldValue);
        }
        return h;
    }

    @Override
    public Object[] inputFieldsToString(W w) {
        Object[] a = new Object[inputFieldNames.getLength() * 2];
        int j = 0;
        for (int i = 0; i < inputFieldNames.getLength(); i++) {
            JSString fieldName = inputFieldNames.get(i);
            a[j++] = fieldName;
            a[j++] = JSWrapper.maybeWrap(getInputFieldValue(w, fieldName));
        }
        return a;
    }

    @Override
    public Widget decorate(W w, @NonNull Widget content) {
        // TODO duplicate provider típusokat detektálni kéne
        for (int i = 0; i < providerFieldNames.getLength(); i++) {
            JSString fieldName = providerFieldNames.get(i);
            Class<?> providerType = providerTypes.get(i);

            Observable<?> o = (Observable<?>) getProviderFieldValue(w, fieldName);
            if (o == null)
                throw new NullPointerException(clazz.getSimpleName() + "." + fieldName + " is null " +
                        "(instead of an " + Observable.class.getSimpleName() + ")");
            Object value = o.get();
            content = wrapInProvider(content, providerType, value);
        }
        for (int i = 0; i < providerMethods.getLength(); i++) {
            JSObject method = providerMethods.get(i);
            Class<?> providerType = providerTypes.get(providerFieldNames.getLength() + i);

            Object value = invokeL(method, (JSObject) w);
            content = wrapInProvider(content, providerType, value);
        }
        return content;
    }

    @Override
    public WidgetAccessor<W> asDetachedMarker(boolean detached) {
        if (this.isDetachedMarker == detached)
            return this;
        else
            return other;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for " + clazz.getName() +
                (isDetachedMarker ? " [detached marker]" : "");
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TeaVMWidgetAccessor<?> a && asDetachedMarker(false) == a.asDetachedMarker(false);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(asDetachedMarker(false));
    }

    private static <T> @NonNull Widget wrapInProvider(@NonNull Widget content, Class<T> type, Object value) {
        return new Provider<>(type, type.cast(value), content);
    }


    // azért nem JSObject a return typejuk, mert úgy a RESULT DependencyNodera nem lehet rárakni typeokat,
    // mert csak JSObject subtypejait engedné
    private static Object getInputFieldValue(Object obj, JSString fieldName) {
        // JSWrapper.dependencyJsToJava kb. reinterpret_cast Object-re
        return JSWrapper.dependencyJsToJava(JS.get(JSWrapper.dependencyJavaToJs(obj), fieldName));
    }

    private static Object getInjectFieldValue(Object obj, JSString fieldName) {
        return JSWrapper.dependencyJsToJava(JS.get(JSWrapper.dependencyJavaToJs(obj), fieldName));
    }

    private static Object getStateFieldValue(Object obj, JSString fieldName) {
        return JSWrapper.dependencyJsToJava(JS.get(JSWrapper.dependencyJavaToJs(obj), fieldName));
    }

    private static Object getProviderFieldValue(Object obj, JSString fieldName) {
        return JSWrapper.dependencyJsToJava(JS.get(JSWrapper.dependencyJavaToJs(obj), fieldName));
    }

    // ha ezt használjuk egy fieldre, akkor egészítsük ki a lenti plugint azzal, hogy hozzáad egy
    // dependency-t a fieldhez
    private static void fieldSet(Object obj, JSString fieldName, Object value) {
        JS.set(JSWrapper.dependencyJavaToJs(obj), fieldName, JSWrapper.dependencyJavaToJs(value));
    }

    /**
     * ha virtual call kell, akkor natív string legyen az első paraméter típusa, különben function
     */
    @JSBody(script = "return typeof method === 'string' ? obj[method]() : method(obj);", params = {"method", "obj"})
    private static native Object invokeL(JSObject method, JSObject obj);

    @JSBody(script = "return method(arg1, arg2, arg3, arg4);", params = {"method", "arg1", "arg2", "arg3", "arg4"})
    private static native Object invokeLLIL(JSObject method, JSObject arg1, JSObject arg2, int arg3, JSObject arg4);

    /**
     * PlatformClass.getJavaClass nem jó, mert az csak kiolvassa a .classObject mezőt a natív classból, de nem
     * inicializálja, ha null
     */
    @JSBody(script = "return $rt_cls(nativeClass);", params = {"nativeClass"})
    private static native Class<?> nativeClassToJavaClass(JSObject nativeClass);

    static {
        initAccessors();
    }

    private static native void initAccessors();

    private static final String WIDGET_ACCESSOR_VAR_NAME = "__WACC";

    public static <W extends Widget> TeaVMWidgetAccessor<W> accessorFor(Class<W> widgetType) {
        PlatformClass widgetTypePlatformClass = platformClassForJavaClass(widgetType);
        TeaVMWidgetAccessor<W> accessor = getAccessorImpl(widgetTypePlatformClass);
        if (accessor == null)
            throw new RuntimeException("can't get accessor for " + widgetType.getName());
        return accessor;
    }

    // || null azért, hogy undefined ne szivárogjon be
    @JSBody(script = "return widgetType." + WIDGET_ACCESSOR_VAR_NAME + " || null;", params = {"widgetType"})
    private static native <W extends Widget> TeaVMWidgetAccessor<W> getAccessorImpl(PlatformClass widgetType);

    // DependencyListenerben is hivatkozunk rá
    @InjectedBy(PlatformClassForJavaClassInjector.class)
    private static native PlatformClass platformClassForJavaClass(Class<?> clazz);

    @JSBody(script = "clazz." + WIDGET_ACCESSOR_VAR_NAME + " = accessor;",
            params = {"clazz", "accessor"})
    private static native <W extends Widget> Class<W> putWidgetAccessor(JSObject clazz, JSObject accessor);

    // publikusnak kell lennie, mert különben másik classloaderből nem engedné betölteni
    public static class WidgetAccessorTeaVMPLugin implements TeaVMPlugin {

        private final List<WidgetDefinitionParser> widgetTypes = new ArrayList<>();
        private final Set<MethodReference> forceVirtual = new HashSet<>();
        private final Map<Class<?>, MethodReference> interfaceProxyFactories = new HashMap<>();

        private static final MethodReference CONSTRUCTOR_METHOD_REF = new MethodReference(TeaVMWidgetAccessor.class,
                "make",
                JSObject.class, // natív class

                JSArray.class, // inputFieldNames
                int.class, // finalInputFieldCount
                JSArray.class, // interfaceProxyInputFieldTypes
                JSArray.class, // injectFieldNames
                JSArray.class, // injectFieldTypes
                JSArray.class, // injectFieldInterfaceProxyFactories
                int.class, // injectFieldsOptional
                int.class, // injectFieldsInherited
                int.class, // injectFieldsObservableWrapped
                JSArray.class, // stateFieldNames
                JSArray.class, // stateFieldZeroes
                JSArray.class, // providerFieldNames
                JSArray.class, // providerMethods
                JSArray.class, // providerTypes
                void.class
        );

        @Override
        public void install(TeaVMHost host) {
            // minden provider metódus legyen virtuális. ez nem jó, mert elveszik deoptimization optimalizáció,
            // de nincs publikus API TeaVM-ben, ami megmondja hogy egy metódus virtuális-e.
            host.getExtension(TeaVMJavaScriptHost.class).addVirtualMethods(
                    (context, methodRef) -> forceVirtual.contains(methodRef));

            host.add(new DependencyListener() {

                private boolean constructorUseAdded;

                @Override
                public void started(DependencyAgent agent) {
                }

                @Override
                public void classReached(DependencyAgent agent, String className) {
                    Class<?> c;
                    try {
                        c = agent.getClassLoader().loadClass(className);
                    } catch (ClassNotFoundException e) {
                        // lambda vagy hasonló
                        return;
                    }
                    if (WidgetDefinitionParser.isWidgetType(c)) {
                        Class<? extends Widget> c1 = c.asSubclass(Widget.class);
                        WidgetDefinitionParser r = new WidgetDefinitionParser(c1);
                        r.reflect();
                        widgetTypes.add(r);

                        if (!constructorUseAdded) {
                            agent.linkMethod(CONSTRUCTOR_METHOD_REF).use();
                            constructorUseAdded = true;
                        }
                        // TODO a linkField önmagában csinál bármit? meg mi az a use?
                        for (Field f : r.inputFields) {
                            FieldDependency field = agent.linkField(
                                    new FieldReference(f.getDeclaringClass().getName(), f.getName()));
                            DependencyNode fieldGet =
                                    agent.linkMethod(new MethodReference(TeaVMWidgetAccessor.class,
                                                    "getInputFieldValue",
                                                    Object.class, JSString.class, Object.class)).
                                            getResult();

                            field.getValue().connect(fieldGet);
                        }
                        for (StateFieldInfo f : r.stateFields) {
                            FieldDependency field = agent.linkField(
                                    new FieldReference(f.field().getDeclaringClass().getName(), f.field().getName()));
                            if (f.isObservable())
                                // TODO nem kéne hivatkoznunk másik modulban lévő privát osztály nevére
                                field.getValue().propagate(agent.getType(ValueType.object(
                                        "ui11.observable.ObservableImpl$NullableObservableImpl")));

                            DependencyNode fieldGet =
                                    agent.linkMethod(new MethodReference(TeaVMWidgetAccessor.class,
                                                    "getStateFieldValue",
                                                    Object.class, JSString.class, Object.class)).
                                            getResult();
                            field.getValue().connect(fieldGet);
                        }
                        for (InjectionFieldInfo f : r.injectFields) {
                            agent.linkClass(f.type().getName()); // DynamicProvider miatt

                            FieldDependency fieldDependency = agent.linkField(
                                    new FieldReference(f.field().getDeclaringClass().getName(),
                                            f.field().getName()));

                            ValueType fieldType = ValueType.parse(f.type());
                            DependencyType type = agent.getType(fieldType);
                            switch (f.kind()) {
                                case NORMAL -> {
                                    DependencyNode allInjectedTypes =
                                            agent.linkMethod(Provider.class.getName(),
                                                    new MethodDescriptor("value", Object.class)).getResult();
                                    allInjectedTypes.connect(fieldDependency.getValue(), new DependencyTypeFilter() {
                                        @Override
                                        public boolean match(DependencyType type) {
                                            return agent.getClassHierarchy().isSuperType(
                                                    fieldType, type.getValueType(), true);
                                        }
                                    });
                                }
                                case SLOT_OR_MULTI_SLOT -> {
                                    fieldDependency.getValue().propagate(type);
                                }
                                case OBSERVABLE -> {
                                    fieldDependency.getValue().propagate(agent.getType(
                                            ValueType.object(InheritedProp.class.getName())));
                                }
                                case INTERFACE_PROXY -> {
                                    if (!interfaceProxyFactories.containsKey(f.type())) {
                                        InheritedInterfaceProxyGenerator g = new InheritedInterfaceProxyGenerator(f.type());
                                        agent.submitClassFile(g.toClassfile());

                                        DirectMethodHandleDesc factoryMethod = g.factoryMethod();
                                        MethodReference factoryMethodRef = toMethodReference(factoryMethod);
                                        interfaceProxyFactories.put(f.type(), factoryMethodRef);

                                        agent.linkMethod(factoryMethodRef).use();
                                    }

                                    // feltesszük hogy a proxy osztálya ugyanaz, mint a factory metódus declaring classa
                                    // TODO inkább kérjük el InheritedInterfaceProxyGeneratortól
                                    fieldDependency.getValue().propagate(agent.getType(ValueType.object(
                                            interfaceProxyFactories.get(f.type()).getClassName())));
                                }
                            }

                            if (f.kind() != InjectionFieldInfo.InjectedFieldKind.SLOT_OR_MULTI_SLOT) {
                                DependencyNode inheritedPropConstructorTypeParam =
                                        agent.linkMethod(new MethodReference(InheritedProp.class, "<init>",
                                                        WidgetState.class, Class.class, boolean.class, String.class,
                                                        void.class)).
                                                getVariable(2 /* type paraméter */);
                                inheritedPropConstructorTypeParam.getClassValueNode().propagate(type);
                            }

                            DependencyNode getInjectFieldValue =
                                    agent.linkMethod(new MethodReference(TeaVMWidgetAccessor.class,
                                                    "getInjectFieldValue",
                                                    Object.class, JSString.class, Object.class)).
                                            getResult();
                            fieldDependency.getValue().connect(getInjectFieldValue);
                        }

                        // lehetne Element.findInheritedValue#RESULT-ba, és akkor nem szemetelnénk tele mindent
                        // ami Map.get-et használ
                        DependencyNode ivValueDepNode =
                                agent.linkMethod(new MethodReference(Provider.class, "<init>",
                                                Class.class, Object.class, Widget.class, void.class)).
                                        getVariable(2 /* value paraméter */);

                        for (ProviderMethodInfo<?> p : r.providers) {
                            switch (p.member()) {
                                case Method m -> {
                                    MethodReference methodRef = new MethodReference(m.getDeclaringClass(),
                                            m.getName(), methodDesc(m));
                                    // private metódusoknál hiába adok vissza true-t rá MethodContributorból,
                                    // akkor se lesz virtuális.
                                    // TODO finalnál is ez a helyzet?
                                    if (!Modifier.isPrivate(m.getModifiers()))
                                        forceVirtual.add(methodRef);
                                    MethodDependency methodDependency = agent.linkMethod(methodRef);
                                    methodDependency.use();
                                    //System.out.println("connect provider method to dep node:  "+methodDependency
                                    // .getResult().getTag() + " to "+ivValueDepNode.getTag());
                                    methodDependency.getResult().connect(ivValueDepNode);
                                }
                                case Field f -> {
                                    agent.linkField(new FieldReference(
                                                    f.getDeclaringClass().getName(), f.getName())).
                                            getValue().connect(ivValueDepNode);
                                }
                                default -> {
                                    throw new RuntimeException("unknown member for " + p);
                                }
                            }

                            // getProviderFieldValue-hoz nem kell propagateType, mert ivValueDepNode-hoz már
                            // megcsináltuk
                        }
                    }
                }

                private static @NonNull MethodReference toMethodReference(DirectMethodHandleDesc factoryMethod) {
                    return new MethodReference(
                            ReflectionUtil.name(factoryMethod.owner()),
                            new MethodDescriptor(factoryMethod.methodName(),
                                    MethodDescriptor.parseSignature(factoryMethod.lookupDescriptor())));
                }

                @Override
                public void methodReached(DependencyAgent agent, MethodDependency method) {
                    if (method.getMethod().getName().equals("platformClassForJavaClass") &&
                            method.getMethod().getOwnerName().equals(TeaVMWidgetAccessor.class.getName()))
                        agent.linkMethod(PlatformClassForJavaClassInjector.Class_getPlatformClass).use();
                    if (method.getMethod().getOwnerName().equals(TeaVMWidgetAccessor.class.getName()) &&
                            method.getMethod().getName().equals("makeObservableWithInitial")) {
                        for (String fieldName : List.of("ZERO_Z", "ZERO_B", "ZERO_S", "ZERO_C",
                                "ZERO_I", "ZERO_F", "ZERO_J", "ZERO_D", "NULL_OBSERVABLE_MARKER")) {
                            FieldReference fieldRef = new FieldReference(TeaVMWidgetAccessor.class.getName(), fieldName);
                            FieldDependency fieldDependency = agent.linkField(fieldRef);
                            fieldDependency.getValue().connect(method.getVariable(0));
                        }
                    }
                }

                @Override
                public void fieldReached(DependencyAgent agent, FieldDependency field) {
                }

                @Override
                public void completing(DependencyAgent agent) {
                }

                @Override
                public void complete() {
                }
            });
            host.getExtension(TeaVMJavaScriptHost.class).add(
                    new MethodReference(TeaVMWidgetAccessor.class, "initAccessors", void.class),
                    new InitAccessorsGenerator());
        }

        private static Class<?> @NonNull [] methodDesc(Method m) {
            Class<?>[] paramsAndReturnType = Arrays.copyOf(m.getParameterTypes(), m.getParameterCount() + 1);
            paramsAndReturnType[paramsAndReturnType.length - 1] = m.getReturnType();
            return paramsAndReturnType;
        }

        private class InitAccessorsGenerator implements Generator {
            @Override
            public void generate(GeneratorContext context, SourceWriter writer,
                                 MethodReference methodRef) {
                if (widgetTypes.isEmpty())
                    return;

                System.out.println("initAcc");
                for (WidgetDefinitionParser r : widgetTypes) {

                    writer.append("// ").append(r.clazz.getName()).newLine();

                    writer.appendMethod(CONSTRUCTOR_METHOD_REF);
                    writer.append("(");
                    writer.newLine();
                    writer.indent();

                    writer.appendClass(r.clazz).append(",").newLine();

                    writer.append('[');
                    for (int i = 0; i < r.inputFields.size(); i++) {
                        Field f = r.inputFields.get(i);
                        if (i != 0)
                            writer.append(", ");
                        writer.append('"');
                        writer.appendField(new FieldReference(f.getDeclaringClass().getName(),
                                f.getName()));
                        writer.append('"');
                    }
                    writer.append("], // inputFieldNames").newLine();

                    int finalInputFieldCount = -1; // TODO
                    writer.append(finalInputFieldCount).append(", // finalInputFieldCount").newLine();

                    writer.append('[');
                    // TODO
                    writer.append("], // interfaceProxyInputFieldTypes").newLine();

                    writer.append('[');
                    for (int i = 0; i < r.injectFields.size(); i++) {
                        InjectionFieldInfo f = r.injectFields.get(i);
                        if (i != 0)
                            writer.append(", ");
                        writer.append('"');
                        writer.appendField(new FieldReference(f.field().getDeclaringClass().getName(),
                                f.field().getName()));
                        writer.append('"');
                    }
                    writer.append("], // injectFieldNames").newLine();

                    int injectFieldsOptional = 0, injectFieldsInherited = 0, injectFieldsObservableWrapped = 0;

                    writer.append('[');
                    for (int i = 0; i < r.injectFields.size(); i++) {
                        InjectionFieldInfo f = r.injectFields.get(i);
                        if (i != 0)
                            writer.append(", ");
                        writer.appendClass(f.type());

                        if (f.optional())
                            injectFieldsOptional |= 1 << i;
                        switch (f.kind()) {
                            case NORMAL, INTERFACE_PROXY -> {
                                injectFieldsInherited |= 1 << i;
                            }
                            case OBSERVABLE -> {
                                injectFieldsInherited |= 1 << i;
                                injectFieldsObservableWrapped |= 1 << i;
                            }
                            case SLOT_OR_MULTI_SLOT -> {
                                // nop
                            }
                            default -> {
                                throw new RuntimeException("Unknown injection field kind: " + f);
                            }
                        }
                    }
                    writer.append("], // injectFieldTypes").newLine();

                    writer.append('[');
                    for (int i = 0; i < r.injectFields.size(); i++) {
                        InjectionFieldInfo f = r.injectFields.get(i);
                        if (i != 0)
                            writer.append(", ");

                        if (f.kind() == InjectionFieldInfo.InjectedFieldKind.INTERFACE_PROXY) {
                            writer.appendMethod(interfaceProxyFactories.get(f.type()));
                        } else {
                            writer.append("null");
                        }
                    }
                    writer.append("], // injectFieldInterfaceProxyFactories").newLine();

                    writer.append(injectFieldsOptional).append(", // injectFieldsOptional").newLine();
                    writer.append(injectFieldsInherited).append(", // injectFieldsInherited").newLine();
                    writer.append(injectFieldsObservableWrapped).append(", // injectFieldsObservableWrapped").newLine();

                    writer.append('[');
                    for (int i = 0; i < r.stateFields.size(); i++) {
                        Field f = r.stateFields.get(i).field();
                        if (i != 0)
                            writer.append(", ");
                        writer.append('"');
                        writer.appendField(new FieldReference(f.getDeclaringClass().getName(), f.getName()));
                        writer.append('"');
                    }
                    writer.append("], // stateFieldNames").newLine();

                    writer.append('[');
                    for (int i = 0; i < r.stateFields.size(); i++) {
                        StateFieldInfo f = r.stateFields.get(i);
                        if (i != 0)
                            writer.append(", ");

                        if (f.isObservable()) {
                            switch (f.zeroValueOfObservable()) {
                                case Boolean _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_Z"));
                                case Byte _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_B"));
                                case Short _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_S"));
                                case Character _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_C"));
                                case Integer _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_I"));
                                case Float _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_F"));
                                case Double _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_D"));
                                case Long _ -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "ZERO_J"));
                                case null -> writer.appendStaticField(new FieldReference(
                                        TeaVMWidgetAccessor.class.getName(), "NULL_OBSERVABLE_MARKER"));
                                default ->
                                        throw new RuntimeException("unknown default value for observable state field: " +
                                                f + ", " + f.zeroValueOfObservable());
                            }
                        } else {
                            // TOOD itt lehetne helyette használni StateFieldInfo.zeroValuet
                            if (f.field().getType().isPrimitive())
                                if (f.field().getType() == long.class)
                                    // BigIntként van implementálva long, így működni fog rajta ==
                                    writer.appendGlobal("Long_ZERO");
                                else // int ugyanaz JS-ben mint double. boolean helyett pedig 0/1-et használ TeaVM
                                    writer.append(0);
                            else
                                writer.append("null");
                        }
                    }
                    writer.append("], // stateFieldZeroes").newLine();

                    List<ProviderMethodInfo<?>> providerFields = r.providers.stream().
                            filter(p -> p.member() instanceof Field).toList();
                    List<ProviderMethodInfo<?>> providerMethods = r.providers.stream().
                            filter(p -> p.member() instanceof Method).toList();

                    if (providerFields.size() + providerMethods.size() != r.providers.size())
                        throw new RuntimeException(r.clazz.getName() + " has provider which is not a field or method:" +
                                r.providers);

                    List<Class<?>> providersTypes = new ArrayList<>();

                    writer.append('[');
                    for (int i = 0; i < providerFields.size(); i++) {
                        ProviderMethodInfo<?> p = providerFields.get(i);
                        if (i != 0)
                            writer.append(", ");
                        writer.append('"');
                        writer.appendField(new FieldReference(((Field) p.member()).getDeclaringClass().getName(),
                                ((Field) p.member()).getName()));
                        writer.append('"');

                        providersTypes.add(p.providedType());
                    }
                    writer.append("], // providerFieldNames").newLine();

                    writer.append('[');
                    for (int i = 0; i < providerMethods.size(); i++) {
                        ProviderMethodInfo<?> p = providerMethods.get(i);
                        Method m = (Method) p.member();
                        final Class<?>[] paramsAndReturnType = methodDesc(m);

                        if (i != 0)
                            writer.append(", ");

                        if (Modifier.isStatic(m.getModifiers()) || Modifier.isFinal(m.getModifiers()) ||
                                Modifier.isPrivate(m.getModifiers())) {
                            MethodReference methodReference = new MethodReference(
                                    m.getDeclaringClass(), m.getName(), paramsAndReturnType);
                            writer.appendMethod(methodReference);
                        } else {
                            writer.append('"');
                            writer.appendVirtualMethod(new MethodDescriptor(m.getName(), paramsAndReturnType));
                            writer.append('"');
                        }

                        providersTypes.add(p.providedType());
                    }
                    writer.append("], // providerMethodNames").newLine();

                    writer.append('[');
                    for (int i = 0; i < providersTypes.size(); i++) {
                        Class<?> t = providersTypes.get(i);
                        if (i != 0)
                            writer.append(", ");

                        writer.appendClass(t);
                    }
                    writer.append("] // providerTypes").newLine();

                    writer.outdent();
                    writer.append(")");
                    writer.append(";").newLine();
                    writer.newLine();
                }
            }
        }
    }

    public static class PlatformClassForJavaClassInjector implements Injector {

        static final MethodReference Class_getPlatformClass = new MethodReference(
                Class.class, "getPlatformClass", PlatformClass.class);

        @Override
        public void generate(InjectorContext context, MethodReference methodRef) {
            InvocationExpr expr = new InvocationExpr();
            expr.setType(InvocationType.SPECIAL);
            expr.setMethod(Class_getPlatformClass);
            expr.getArguments().add(context.getArgument(0));
            context.writeExpr(expr);
        }
    }
}
