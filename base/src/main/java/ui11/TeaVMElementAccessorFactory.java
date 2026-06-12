package ui11;

import org.teavm.metaprogramming.*;
import org.teavm.metaprogramming.reflect.ReflectField;
import org.teavm.metaprogramming.reflect.ReflectMethod;
import ui11.ElementDefReflector.DecoratorMetadata;
import ui11.ElementDefReflector.InjectionFieldInfo;
import ui11.ElementDefReflector.InputFieldInfo;
import ui11.observable.Observable;
import ui11.reflectutil.ReflectionUtil;

import javax.annotation.Nonnull;
import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// ha ezt módosítjuk, módosítsuk RegularWidgetAccessort is.
// érdemesebb először azt módosítani és utána ezt, mert az átláthatóbb.
@CompileTime
class TeaVMElementAccessorFactory {

    private static final int BUCKETS = 8;

    static Class<?> getTeaVMSupportClassOfHandler(Class<?> handlerClass) {
        if (TeaVMElementAccessorFactory.class.getClassLoader() instanceof URLClassLoader)
            throw new RuntimeException("wrong classloader of " + TeaVMElementAccessorFactory.class.getName() + ": " +
                    TeaVMElementAccessorFactory.class.getClassLoader());

        Class<?> teavmSupportClass;
        try {
            // így próbáljuk kikényszeríteni, hogy jó classloaderrel töltődjön be az az osztály, amiben
            // a Metaprogramming.* hívások vannak
            teavmSupportClass = Class.forName(handlerClass.getName() + "$TeaVMSupport");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("TODO", e);
        }
        if (teavmSupportClass.getClassLoader() instanceof URLClassLoader)
            throw new RuntimeException("Wrong classloader of " + teavmSupportClass.getName() + ": " +
                    teavmSupportClass.getClassLoader() + ". " +
                    "Probably missing @" + CompileTime.class.getName() + " annotation");
        return teavmSupportClass;
    }

    // azért muszáj, mert nem működik Metaprogramming.proxy-ban az equals.
    // de amúgyis is jó, mert nem kell végigmenni a bucketek miatti hashcodeon meg hosszú if-else láncokon, hogy
    // megtaláljuk az accessort.
    private static final Map<Class<? extends Widget>, WidgetAccessor<? extends Widget>> CACHE = new HashMap<>();

    static <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM(Class<T> clazz) {
        // computeIfAbsent nem jó, mert TeaVM elfelejti addigra hogy T extends Widget
        @SuppressWarnings("unchecked") WidgetAccessor<T> accessor = (WidgetAccessor<T>) CACHE.get(clazz);
        if (accessor == null)
            CACHE.put(clazz, accessor = doGetElementAccessor(clazz));
        return accessor;
    }

    private static <T extends Widget> @Nonnull WidgetAccessor<T> doGetElementAccessor(Class<T> c) {
        WidgetAccessor<T> accessor = switch (Math.floorMod(c.getName().hashCode(), BUCKETS)) {
            case 0 -> getElementAccessor_TeaVM_0(c);
            case 1 -> getElementAccessor_TeaVM_1(c);
            case 2 -> getElementAccessor_TeaVM_2(c);
            case 3 -> getElementAccessor_TeaVM_3(c);
            case 4 -> getElementAccessor_TeaVM_4(c);
            case 5 -> getElementAccessor_TeaVM_5(c);
            case 6 -> getElementAccessor_TeaVM_6(c);
            case 7 -> getElementAccessor_TeaVM_7(c);
            default -> throw new RuntimeException();
        };
        if (accessor == null)
            throw new RuntimeException("no accessor for " + c.getName());
        return accessor;
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_0(Class<T> c);

    private static void getElementAccessor_TeaVM_0(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 0);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_1(Class<T> c);

    private static void getElementAccessor_TeaVM_1(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 1);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_2(Class<T> c);

    private static void getElementAccessor_TeaVM_2(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 2);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_3(Class<T> c);

    private static void getElementAccessor_TeaVM_3(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 3);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_4(Class<T> c);

    private static void getElementAccessor_TeaVM_4(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 4);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_5(Class<T> c);

    private static void getElementAccessor_TeaVM_5(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 5);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_6(Class<T> c);

    private static void getElementAccessor_TeaVM_6(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 6);
    }

    @org.teavm.metaprogramming.Meta
    private static native <T extends Widget> WidgetAccessor<T> getElementAccessor_TeaVM_7(Class<T> c);

    private static void getElementAccessor_TeaVM_7(ReflectClass<? extends Widget> edClass) {
        getElementAccessor_TeaVM_impl(edClass, 7);
    }

    @SuppressWarnings({"Convert2MethodRef", "unchecked"})
    private static <W extends Widget> void getElementAccessor_TeaVM_impl(ReflectClass<W> edClass,
                                                                         int bucket) {
        if (Math.floorMod(edClass.getName().hashCode(), BUCKETS) != bucket) {
            Metaprogramming.unsupportedCase();
            return;
        }

        ElementDefReflector def = edDef(edClass);
        if (def == null) {
            // ez akkor van ha TeaVM-specifikus az osztály, de
            // akkor nyilván se nem Node leszármazott, és nincs is
            // @DefaultPeer vagy hasonló annotációja

            Metaprogramming.unsupportedCase();
            return;
        }

        Value<WidgetAccessor<W>> v = (Value<WidgetAccessor<W>>) (Value<?>)
                Metaprogramming.proxy(WidgetAccessor.class, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "clazz" -> {
                            ReflectClass<?> c = Metaprogramming.findClass(def.clazz);
                            Metaprogramming.exit(() -> c.asJavaClass());
                        }
                        case "decorate" -> {
                            Value<?> w = args[0], content = args[1], isDelegate = args[2];
                            emitDecorate(def, (Value<Widget>) w, (Value<Widget>) content, (Value<Boolean>) isDelegate);
                        }
                        case "initAndCopyState" -> {
                            Value<?> oldWidget = args[0], newWidget = args[1];
                            emitInitAndCopyState(def, edClass, (Value<W>) oldWidget, (Value<W>) newWidget);
                        }
                        case "observeInheritedValues" -> {
                            Value<?> stateHolder = args[0];
                            emitObserveInheritedValues(def, (Value<RSWStateHolder<W>>) stateHolder);
                        }
                        case "checkStateEmpty" -> {
                            Value<?> widget = args[0];
                            emitCheckStateEmpty(def, edClass, (Value<W>) widget);
                        }
                        case "inputFieldsEquals" -> {
                            Value<?> a = args[0], b = args[1];
                            emitInputFieldsEquals(def, edClass, (Value<W>) a, (Value<W>) b);
                        }
                        case "inputFieldsEqualsAndTransferListeners" -> {
                            Value<?> a = args[0], b = args[1];
                            emitInputFieldsEqualsAndTransferListeners(def, edClass, (Value<W>) a, (Value<W>) b);
                        }
                        case "inputFieldsHashCode" -> {
                            Value<?> w = args[0];
                            emitInputFieldsHashCode(def, edClass, (Value<W>) w);
                        }
                        case "inputFieldsToString" -> {
                            Value<?> w = args[0];
                            emitInputFieldsToString(def, edClass, (Value<W>) w);
                        }
                        default -> {
                            throw new RuntimeException("unsupported " + WidgetAccessor.class.getSimpleName() + " " +
                                    "function: " + method.getName());
                        }
                    }
                });
        Metaprogramming.exit(() -> v.get());
    }

    @SuppressWarnings("DataFlowIssue")
    static ReflectField findField(Field field, ReflectClass<?> c) {
        ReflectClass<?> c1 = c;
        for (; c != null; c = c.getSuperclass()) {
            if (c.getName().equals(field.getDeclaringClass().getName()))
                for (ReflectField f : c.getDeclaredFields()) {
                    // TODO ez így összekever egy int típusú fieldet egy olyan fielddel, aminek a
                    //      típusa egy olyan osztály, aminek az a neve hogy "int"
                    if (f.getName().equals(field.getName()) &&
                            f.getType().getName().equals(field.getType().getName()))
                        return f;
                }
        }
        throw new RuntimeException("field not found: " + field.getDeclaringClass().getName() + " " + field.getName() +
                " " + field.getType().getName() + " in " + c1.getName());
    }

    public static ReflectMethod findMethod(Executable method) {
        ReflectClass<?> c1 = Metaprogramming.findClass(method.getDeclaringClass());

        return findMethodIn(method, c1);
    }

    @Nonnull
    public static ReflectMethod findMethodIn(Executable method, ReflectClass<?> c1) {
        String methodName = switch (method) {
            case Method m -> m.getName();
            case Constructor<?> c -> "<init>";
        };
        Class<?> methodReturnType = switch (method) {
            case Method m -> m.getReturnType();
            case Constructor<?> c -> void.class;
        };

        ReflectClass<?>[] paramTypes = new ReflectClass[method.getParameterCount()];
        Class<?>[] nativeParamTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++)
            paramTypes[i] = Metaprogramming.findClass(nativeParamTypes[i]);
        ReflectClass<?> returnType = Metaprogramming.findClass(methodReturnType);

        for (ReflectMethod m : c1.getDeclaredMethods()) {
            if (methodName.equals(m.getName()) &&
                    Arrays.equals(paramTypes, m.getParameterTypes()) &&
                    returnType.equals(m.getReturnType()))
                return m;
        }
        throw new RuntimeException("method not found: " + method.getDeclaringClass().getName() +
                " " + methodName +
                " " + Arrays.toString(method.getParameterTypes()));
    }

    public static ReflectMethod findConstructor(Constructor<?> method) {
        ReflectClass<?> c1 = Metaprogramming.findClass(method.getDeclaringClass());
        ReflectClass<?>[] paramTypes = new ReflectClass[method.getParameterCount()];
        Class<?>[] nativeParamTypes = method.getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++)
            paramTypes[i] = Metaprogramming.findClass(nativeParamTypes[i]);

        for (ReflectMethod m : c1.getDeclaredMethods()) {
            if (m.getName().equals("<init>") && Arrays.equals(paramTypes, m.getParameterTypes()))
                return m;
        }
        throw new RuntimeException("constructor not found: " + method.getDeclaringClass().getName() +
                " " + Arrays.toString(method.getParameterTypes()));
    }


    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitDecorate(ElementDefReflector def,
                                                        Value<? extends Widget> container,
                                                        Value<? extends Widget> content,
                                                        Value<Boolean> isDelegate) {
        int decoratorIndex = 0;
        for (DecoratorMetadata d : def.decorators) {
            int decoratorIndexFinal = decoratorIndex++;
            if (d.decoratorReflector().neededForAllChildren())
                content = d.decoratorReflector().makeTeaVMCode(content, container, decoratorIndexFinal);
            else {
                Value<? extends Widget> contentFinal = content;
                Value<? extends Widget> wrapped = Metaprogramming.lazyFragment(() ->
                        d.decoratorReflector().makeTeaVMCode(contentFinal, container, decoratorIndexFinal));
                content = Metaprogramming.emit(() -> {
                    if (isDelegate.get())
                        return wrapped.get();
                    else
                        return contentFinal.get();
                });
            }
        }
        Value<? extends Widget> decorated = content;
        Metaprogramming.exit(() -> decorated.get());
    }

    private static <W extends Widget> void emitInitAndCopyState(ElementDefReflector def,
                                                                ReflectClass<W> teavmClass,
                                                                Value<W> oldWidgetV,
                                                                Value<W> newWidgetV) {
        for (Field stateField : def.stateFields) {
            ReflectField f = findField(stateField, teavmClass);

            Value<?> defaultValue = zero(stateField.getType());

            String tamperedStateFieldOnNewWidgetMsg = makeTamperedStateFieldMsg(def, stateField);
            Metaprogramming.emit(() -> {
                Widget oldWidget = oldWidgetV.get(), newWidget = newWidgetV.get();

                Object zero = defaultValue.get();
                Object newValue = f.get(newWidget);
                checkNotTampered(zero, newValue, tamperedStateFieldOnNewWidgetMsg, newWidget);
                if (oldWidget != null) {
                    Object oldValue = f.get(oldWidget);
                    f.set(newWidget, oldValue);
                }
            });
        }

        Value<?> performInjections = Metaprogramming.lazyFragment(() -> {
            emitPerformInjections(teavmClass, newWidgetV, def);
            return null;
        });
        Metaprogramming.emit(() -> {
            W newWidget = newWidgetV.get();
            if (!newWidget.injectFieldsInitialized) {
                performInjections.get();
                newWidget.injectFieldsInitialized = true;
            }
        });

        for (InputFieldInfo inputField : def.inputFields) {
            ReflectField f = findField(inputField.field(), teavmClass);
            if (!inputField.interfaceProxy())
                continue;

            Value<?> newValueV = Metaprogramming.lazy(() -> f.get(newWidgetV.get()));
            Value<?> makeProxy;

            // azért van a két kasztolás Object-té, mert a lambda dynamicMethodType-ja különben hivatkozna
            // package-privát osztályokra, ami az eltérő classloaderek miatt nem lehetséges

            if (inputField.field().getType() == Runnable.class)
                makeProxy = Metaprogramming.lazy(() -> (Object) new RunnableProxy(newWidgetV.get(),
                        (Runnable) newValueV.get()));
            else if (inputField.field().getType() == Consumer.class)
                makeProxy = Metaprogramming.lazy(() -> (Object) new ConsumerProxy<>(newWidgetV.get(),
                        (Consumer<?>) newValueV.get()));
            else
                throw new RuntimeException("unknown listener type on " +
                        ReflectionUtil.memberToShortString(inputField.field()));

            Metaprogramming.emit(() -> {
                Object newValue = newValueV.get();
                if (newValue == null)
                    return null;

                W oldWidget = oldWidgetV.get(), newWidget = newWidgetV.get();

                Object oldValue = oldWidget == null ? null : f.get(oldWidget);
                if (oldValue != null) {
                    ListenerProxyBase2<?> l = (ListenerProxyBase2<?>) oldValue;
                    l.repurposeForNewWidget(oldWidget, newWidget, newValue);
                    newValue = l;
                } else {
                    newValue = makeProxy.get();
                }

                f.set(newWidget, newValue);
                return null;
            });
        }
    }

    @Nonnull
    private static String makeTamperedStateFieldMsg(ElementDefReflector def, Field stateField) {
        return "The value of field " + ReflectionUtil.memberToShortString(stateField) +
                " was modified before " + def.clazz.getSimpleName() + ".initState() " +
                "of ";
    }

    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitObserveInheritedValues(ElementDefReflector def,
                                                                      Value<RSWStateHolder<W>> stateHolderV) {
        int injectFieldCount = def.injectFields.size();
        if (injectFieldCount == 0) {
            Metaprogramming.exit(() -> EMPTY_OBSERVABLE_ARRAY);
            return;
        }

        Value<Observable<?>[]> ivObsArray = Metaprogramming.emit(() -> new Observable<?>[injectFieldCount]);
        int ivIndex = 0;
        for (InjectionFieldInfo injectionFieldInfo : def.injectFields) {
            int ivIndexFinal = ivIndex++;
            if (injectionFieldInfo.isNotInherited())
                continue;
            ReflectClass<?> ivType = Metaprogramming.findClass(injectionFieldInfo.type());
            boolean optional = injectionFieldInfo.optional();
            Metaprogramming.emit(() -> ivObsArray.get()[ivIndexFinal] =
                    stateHolderV.get().inherited(ivType.asJavaClass(), optional));
        }
        Metaprogramming.exit(() -> ivObsArray.get());
    }

    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitCheckStateEmpty(
            ElementDefReflector def, ReflectClass<W> teavmClass, Value<W> widgetV) {
        for (Field stateField : def.stateFields) {
            ReflectField f = findField(stateField, teavmClass);

            Value<?> defaultValue = zero(stateField.getType());

            String tamperedStateFieldOnNewWidgetMsg = makeTamperedStateFieldMsg(def, stateField);
            Metaprogramming.emit(() -> {
                Object zero = defaultValue.get();
                Object newValue = f.get(widgetV.get());
                checkNotTampered(zero, newValue, tamperedStateFieldOnNewWidgetMsg, widgetV.get());
            });
        }
    }

    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitInputFieldsEquals(
            ElementDefReflector def, ReflectClass<W> teavmClass, Value<W> a, Value<W> b) {
        Value<Boolean> result = Metaprogramming.emit(() -> true);
        for (InputFieldInfo inputField : def.inputFields) {
            ReflectField f = findField(inputField.field(), teavmClass);

            Value<Boolean> prevResult = result;
            result = Metaprogramming.emit(() -> {
                return prevResult.get() & Objects.equals(f.get(a.get()), f.get(b.get()));
            });
        }
        Value<Boolean> prevResult = result;
        Metaprogramming.exit(() -> prevResult.get());
    }


    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitInputFieldsEqualsAndTransferListeners(
            ElementDefReflector def, ReflectClass<W> teavmClass, Value<W> a, Value<W> b) {
        Value<Boolean> result = Metaprogramming.emit(() -> true);
        for (InputFieldInfo inputField : def.inputFields) {
            ReflectField f = findField(inputField.field(), teavmClass);

            Value<Boolean> prevResult = result;
            if (inputField.interfaceProxy())
                result = Metaprogramming.emit(() -> {
                    return prevResult.get() && (f.get(a.get()) == null) == (f.get(b.get()) == null);
                });
            else
                result = Metaprogramming.emit(() -> {
                    return prevResult.get() && Objects.equals(f.get(a.get()), f.get(b.get()));
                });
        }

        if (def.inputFields.stream().anyMatch(i -> i.interfaceProxy())) {
            Value<?> transferListeners = Metaprogramming.lazyFragment(() -> {
                for (InputFieldInfo inputField : def.inputFields) {
                    if (!inputField.interfaceProxy())
                        continue;

                    ReflectField f = findField(inputField.field(), teavmClass);
                    Metaprogramming.emit(() -> {
                        Object aValue = f.get(a.get()), bValue = f.get(b.get());
                        if (aValue != null)
                            ((ListenerProxyBase2<?>) aValue).propagateChangeToOldWidget(a.get(), b.get(), bValue);
                    });
                }
                return Metaprogramming.emit(() -> null);
            });

            Value<Boolean> prevResult = result;
            Metaprogramming.emit(() -> {
                if (prevResult.get())
                    transferListeners.get();
            });
        }

        Value<Boolean> prevResult = result;
        Metaprogramming.exit(() -> prevResult.get());
    }


    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitInputFieldsHashCode(
            ElementDefReflector def, ReflectClass<W> teavmClass, Value<W> w) {
        Value<Integer> result = Metaprogramming.emit(() -> teavmClass.asJavaClass().hashCode());
        for (InputFieldInfo inputField : def.inputFields) {
            ReflectField f = findField(inputField.field(), teavmClass);

            Value<Integer> prevResult = result;
            result = Metaprogramming.emit(() -> {
                return prevResult.get() * 23 + Objects.hashCode(f.get(w.get()));
            });
        }
        Value<Integer> prevResult = result;
        Metaprogramming.exit(() -> prevResult.get());
    }

    @Nonnull
    private static Value<?> zero(Class<?> type) {
        return type.isPrimitive() ? switch (type.getName()) {
            case "boolean" -> Metaprogramming.emit(() -> Boolean.FALSE);
            case "byte" -> Metaprogramming.emit(() -> ZERO_BYTE);
            case "short" -> Metaprogramming.emit(() -> ZERO_SHORT);
            case "char" -> Metaprogramming.emit(() -> ZERO_CHAR);
            case "int" -> Metaprogramming.emit(() -> ZERO_INT);
            case "float" -> Metaprogramming.emit(() -> ZERO_FLOAT);
            case "long" -> Metaprogramming.emit(() -> ZERO_LONG);
            case "double" -> Metaprogramming.emit(() -> ZERO_DOUBLE);
            default -> throw new RuntimeException("unknown primitive type: " + type.getName());
        } : Metaprogramming.emit(() -> null);
    }

    private static <W extends Widget> void emitPerformInjections(ReflectClass<W> clazz,
                                                                 Value<W> w,
                                                                 ElementDefReflector reflector) {
        int ivIndex = 0;
        for (InjectionFieldInfo injectionFieldInfo : reflector.injectFields) {
            int ivIndexFinal = ivIndex++;
            record InjectFieldKey(int index) {}

            ReflectField f = findField(injectionFieldInfo.field(), clazz);

            Value<?> wrapper;
            if (injectionFieldInfo.interfaceProxy()) {
                wrapper = Metaprogramming.proxy(injectionFieldInfo.type(), (proxy, method, args) -> {
                    // TODO Object.equals?

                    int argArrayLen = args.length;
                    Value<Object[]> argArray = Metaprogramming.emit(() -> new Object[argArrayLen]);
                    for (int i = 0; i < argArrayLen; i++) {
                        int iFinal = i;
                        Value<Object> argValue = args[i];
                        Metaprogramming.emit(() -> {
                            argArray.get()[iFinal] = argValue.get();
                        });
                    }
                    Metaprogramming.exit(() -> {
                        Object value = w.get().getInheritedValueByIndex(ivIndexFinal);
                        return method.invoke(value, argArray.get());
                    });
                });
            } else if (injectionFieldInfo.type() == Slot.class) {
                wrapper = Metaprogramming.emit(() -> {
                    return new Slot(w.get(), new InjectFieldKey(ivIndexFinal));
                });
            } else if (injectionFieldInfo.type() == MultiSlot.class) {
                wrapper = Metaprogramming.emit(() -> {
                    return new MultiSlot<>(new Slot(w.get(), new InjectFieldKey(ivIndexFinal)));
                });
            } else {
                wrapper = Metaprogramming.emit(() -> {
                    return makeIVObservable(w.get(), ivIndexFinal);
                });
            }
            Metaprogramming.emit(() -> {
                f.set(w.get(), wrapper.get());
            });
        }
    }

    private static <W extends Widget> void checkNotTampered(Object zero, Object value, String tamperedStateFieldOnNewWidgetMsg, W w) {
        if (!Objects.equals(zero, value)) {
            throw new RuntimeException(tamperedStateFieldOnNewWidgetMsg + w);
        }
    }

    private static final Byte ZERO_BYTE = (byte) 0;
    private static final Short ZERO_SHORT = (short) 0;
    private static final Character ZERO_CHAR = '\0';
    private static final Integer ZERO_INT = 0;
    private static final Float ZERO_FLOAT = 0F;
    private static final Long ZERO_LONG = 0L;
    private static final Double ZERO_DOUBLE = 0D;

    private static Observable<?> makeIVObservable(Widget w, int ivIndex) {
        return Observable.of(() -> w.getInheritedValueByIndex(ivIndex));
    }

    private static final Observable<?>[] EMPTY_OBSERVABLE_ARRAY = new Observable[0];

    @Nonnull
    private static Value<Object[]> objArray(Object[] values) {
        int nArgs = values.length;
        Value<Object[]> arrRef;
        if (values.getClass() == Object[].class)
            arrRef = Metaprogramming.emit(() -> new Object[nArgs]);
        else {
            ReflectClass<?> componentType = Metaprogramming.findClass(values.getClass().componentType());

            @SuppressWarnings({"unchecked", "RedundantCast" /* utóbbi IntelliJ bug */})
            Value<Object[]> arrRef0 = (Value<Object[]>) (Value<?>)
                    Metaprogramming.emit(() -> Array.newInstance(componentType.asJavaClass(), nArgs));
            arrRef = arrRef0;
        }
        for (int i = 0; i < nArgs; i++) {
            int j = i;
            Object val = values[j];
            // TODO primitív tömbök
            if (val instanceof Object[] nestedArray) {
                Value<Object[]> nestedArrayRef = objArray(nestedArray);
                Metaprogramming.emit(() -> arrRef.get()[j] = nestedArrayRef.get());
            } else
                Metaprogramming.emit(() -> arrRef.get()[j] = val);
        }
        return arrRef;
    }

    @SuppressWarnings("Convert2MethodRef")
    private static <W extends Widget> void emitInputFieldsToString(ElementDefReflector def,
                                                                   ReflectClass<W> clazz,
                                                                   Value<W> w) {
        int arrayLength = def.inputFields.size() * 2;
        Value<Object[]> array = Metaprogramming.emit(() -> new Object[arrayLength]);
        List<Integer> order = IntStream.range(0, arrayLength).boxed().
                collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(order, new Random(clazz.getName().hashCode()));
        for (int i : order) {
            InputFieldInfo field = def.inputFields.get(i / 2);
            if ((i % 2) == 0) {
                String fieldName = field.field().getName();
                Metaprogramming.emit(() -> array.get()[i] = fieldName);
            } else {
                ReflectField teavmField = findField(field.field(), clazz);
                Metaprogramming.emit(() -> array.get()[i] = teavmField.get(w));
            }
        }
        Metaprogramming.exit(() -> array.get());
    }


    // nem baj hogy statikus, mert az egész classloader törlődik a fordítás végekor
    private static final Map<String, ElementDefReflector> ELEMENT_DEFS = new ConcurrentHashMap<>();

    static ElementDefReflector edDef(ReflectClass<? extends Widget> c) {
        return ELEMENT_DEFS.computeIfAbsent(c.getName(), name -> {
            Class<? extends Widget> clazz;
            try {
                clazz = Class.forName(name, false, TeaVMElementAccessorFactory.class.getClassLoader()).
                        asSubclass(Widget.class);
            } catch (ClassNotFoundException e) {
                // akkor szokott ilyen lenni, ha TeaVM classlibes osztályt próbálunk betölteni
                return null;
            }
            ElementDefReflector reflector = new ElementDefReflector(clazz, true);
            reflector.reflect();
            return reflector;
        });
    }
}
