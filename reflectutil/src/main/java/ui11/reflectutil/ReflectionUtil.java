package ui11.reflectutil;

import java.lang.StackWalker.Option;
import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;

import static java.lang.invoke.MethodHandles.*;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;

import java.util.stream.Stream;
import javax.annotation.Nonnull;

/**
 *
 */
public class ReflectionUtil {

    public static final StackWalker STACK_WALKER = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

    private ReflectionUtil() {
        throw new AssertionError();
    }

    private static final ClassValue<List<Field>> FIELD_LIST_CV = new ClassValue<List<Field>>() {
        @Override
        protected List<Field> computeValue(Class<?> clazz) {
            Objects.requireNonNull(clazz, "null class");
            List<Field> result = new ArrayList<>();
            if (clazz.getSuperclass() != null)
                result.addAll(fieldsIn(clazz.getSuperclass()));
            result.addAll(asList(clazz.getDeclaredFields()));
            return Collections.unmodifiableList(result);
        }
    };

    /**
     * először a superclassban szereplő fieldeket adja vissza
     */
    public static List<Field> fieldsIn(Class<?> clazz) {
        return FIELD_LIST_CV.get(clazz);
    }

    public static List<Method> methodsIn(Class<?> clazz) {
        // TODO duplicate metódusokat ki kéne szűrni (egy interface többször is szerepelhet az ősök között)
        List<Method> result = new ArrayList<>();
        result.addAll(asList(clazz.getDeclaredMethods()));
        for (Class<?> iface : clazz.getInterfaces())
            result.addAll(methodsIn(iface));
        if (clazz.getSuperclass() != null)
            result.addAll(methodsIn(clazz.getSuperclass()));
        return result;
    }

    public static List<Class<?>> classesIn(Class<?> clazz) {
        List<Class<?>> result = new ArrayList<>();
        result.addAll(asList(clazz.getDeclaredClasses()));
        for (Class<?> iface : clazz.getInterfaces())
            result.addAll(classesIn(iface));
        if (clazz.getSuperclass() != null)
            result.addAll(classesIn(clazz.getSuperclass()));
        return result;
    }

    /**
     * Ősök felől megy a leszármazottak felé a visszaadott lista sorrendje.
     */
    public static List<Method> methodsIn2(Class<?> clazz) {
        List<Method> result = new ArrayList<>();
        if (clazz.getSuperclass() != null)
            result.addAll(methodsIn2(clazz.getSuperclass()));
        for (Class<?> iface : clazz.getInterfaces())
            result.addAll(methodsIn2(iface));
        result.addAll(asList(clazz.getDeclaredMethods()));
        return result;
    }

    public static @Nonnull
    Class<?> rawType(AnnotatedType type) {
        java.lang.reflect.Type genericType = type.getType();
        return rawType(genericType);
    }

    public static Class<?> rawType(java.lang.reflect.Type genericType) {
        Objects.requireNonNull(genericType);
        if (!(genericType instanceof Class)) {
            if (genericType instanceof TypeVariable) {
                java.lang.reflect.Type[] bounds = ((TypeVariable<?>) genericType).getBounds();
                if (bounds.length != 1)
                    return Object.class; // lub kéne különben
                else
                    return rawType(bounds[0]);
            }
            if (genericType instanceof WildcardType wildcardType) {
                return rawType(wildcardType.getUpperBounds()[0]); // TODO nem az elsőt kéne venni
            }
            genericType = ((ParameterizedType) genericType).getRawType();
        }
        Objects.requireNonNull(genericType);
        return (Class<?>) genericType;
    }

    public static String typeToString(AnnotatedType annotatedType) {
        // TODO support AnnotatedParameterizedType
        String result = "";
        for (Annotation annotation : annotatedType.getAnnotations()) {
            result += annotation + " ";
        }

        return result + annotatedType.getType().getTypeName();
    }

    public static void ensureClassInitialized(Class<?> clazz) {
        if (clazz.isPrimitive())
            return;

        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new AssertionError(ex); // shouldn't happen
        }
    }

    // TODO ezt töröljük ki, van helyette az internalName()
    public static String className(Class clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Ha a megadott elem egy osztály, akkor ez a visszatérési érték az lesz, nem pedig egy bennfoglaló osztály.
     */
    public static Class<?> declaringClass(AnnotatedElement element) {
        if (element instanceof Class)
            return (Class<?>) element;

        if (element instanceof Member)
            return ((Member) element).getDeclaringClass();

        if (element instanceof Parameter)
            return declaringClass(((Parameter) element).getDeclaringExecutable());

        throw new IllegalArgumentException(element + ", " + element.getClass());
    }

    /**
     * Ez a declaring class simple name-jét használja, míg a másik a teljeset
     */
    // Itt az elnevezés annyiból szerencsétlen, hogy a declaring class teljes nevét
    // adjuk vissza. De az meg hülyén nézett volna ki, hogy memberToShortStringWithFullDeclaringClassName. 
    // CBLocalizedText-nek kellett ez a működés, viszont configbinderben meg sok helyen 
    // simplename-mel kéne a declaringclass, ezért majd kéne csinálni egy olyan változatot is, 
    // csak nem tudom, minek kéne hívni.
    // 2025-04-06: most már ElementDefReflectorban is az a működés kell (eClassOrMethod miatt),
    //             hogy Class::getName-et adja vissza class esetén
    public static String memberToShortString(AnnotatedElement element) {
        if (element instanceof Class)
            return ((Class) element).getName();

        if (element instanceof Method) {
            Method m = (Method) element;
            return m.getDeclaringClass().getName() + "::" + m.getName();
        }

        if (element instanceof Field) {
            Field f = (Field) element;
            return f.getDeclaringClass().getName() + "." + f.getName();
        }

        if (element instanceof Constructor<?> c) {
            return c.getDeclaringClass().getName() + ".<init>"; // TODO hogy lehetne szebben kijelezni?
        }

        if (element instanceof Parameter)
            return "parameter " + element.toString() + " of "
                    + memberToShortString(((Parameter) element).getDeclaringExecutable());

        if (element instanceof RecordComponent rc)
            return rc.getDeclaringRecord().getName() + "::" + rc.getName();

        // pl. CompositeAnnotatedElement
        // throw new IllegalArgumentException(element + ", " + element.getClass());
        return element.toString();
    }

    /**
     * Ez a declaring class simple name-jét használja, míg a másik a teljeset
     */
    public static String memberToShortString2(AnnotatedElement element) {
        if (element instanceof Class)
            return ((Class) element).getName();

        if (element instanceof Method) {
            Method m = (Method) element;
            return simpleName(m.getDeclaringClass()) + "::" + m.getName();
        }

        if (element instanceof Field) {
            Field f = (Field) element;
            return simpleName(f.getDeclaringClass()) + "." + f.getName();
        }

        if (element instanceof Parameter)
            return "parameter " + element.toString() + " of "
                    + memberToShortString2(((Parameter) element).getDeclaringExecutable());

        if (element instanceof RecordComponent rc)
            return rc.getDeclaringRecord().getSimpleName() + "::" + rc.getName();

        throw new IllegalArgumentException(element + ", " + element.getClass());
    }

    public static String memberKind(AnnotatedElement element) {
        return switch (element) {
            case Class<?> _ -> "class";
            case Method _ -> "method";
            case Field _ -> "field";
            case Constructor<?> _ -> "constructor";
            case Parameter _ -> "parameter";
            case RecordComponent _ -> "record component";
            default -> element.getClass().getName(); // unknown
        };
    }

    public static MethodHandle getterHandle(Field f) throws InaccessibleObjectException {
        f.setAccessible(true);
        try {
            return lookup().unreflectGetter(f);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("should not happen", ex);
        }
    }

    public static MethodHandle handle(Method m) throws InaccessibleObjectException {
        m.setAccessible(true);
        try {
            return lookup().unreflect(m);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("should not happen", ex);
        }
    }

    public static Field lookupField(Class<?> owner, String name) {
        // TODO kéne access check

        List<Field> fields = fieldsIn(owner);
        for (int i = fields.size() - 1; i >= 0; i--) {
            Field f = fields.get(i);
            if (f.getName().equals(name))
                return f;
        }

        throw new RuntimeException("no field named '" + name + "' found in " + owner);
    }

    public static int depth(Class<?> c) {
        if (c.isInterface())
            throw new IllegalArgumentException("interfaces not supported here");

        int d = 0;
        while (c != null) {
            d++;
            c = c.getSuperclass();
        }
        return d;
    }

    public static int distanceToAncestor(Class<?> descendant, Class<?> ancestor) {
        List<Class<?>> q = new ArrayList<>();
        q.add(descendant);
        q.add(null);

        int d = 0;

        while (true) {
            Class<?> c = q.removeFirst();
            if (c == null) {
                if (q.isEmpty())
                    throw new IllegalArgumentException();

                q.add(null);
                d++;
            } else {
                if (c == ancestor) {
                    System.out.println(descendant.getName() + " - " + ancestor.getName() + ": " + d);
                    return d;
                }

                if (c.getSuperclass() != null)
                    q.add(c.getSuperclass());
                q.addAll(asList(c.getInterfaces()));
            }
        }
    }

    public static List<Class<?>> supertypesAndSelf(Class<?> c) {
        List<Class<?>> l = new ArrayList<>();
        l.add(c);
        for (int i = 0; i < l.size(); i++) {
            Class<?> superclass = l.get(i).getSuperclass();
            if (superclass != null && !l.contains(superclass))
                l.add(superclass);
            for (Class<?> superinterface : l.get(i).getInterfaces())
                if (!l.contains(superinterface))
                    l.add(superinterface);
        }
        return l;
    }

    public static ClassValue<List<MethodHandle>> annotatedMethodsCV(Class<? extends Annotation> annType) {
        return new ClassValue<>() {
            @Override
            protected List<MethodHandle> computeValue(Class<?> type) {
                return annotatedMethods(type, annType);
            }
        };
    }

    @Nonnull
    public static List<MethodHandle> annotatedMethods(Class<?> type, Class<? extends Annotation> annType) {
        return methodsIn(type).stream().
                filter(m -> m.isAnnotationPresent(annType)).
                map(m -> {
                    m.setAccessible(true);
                    try {
                        MethodHandle mh = lookup().unreflect(m);
                        return mh.asType(mh.type().changeParameterType(0, type));
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }).
                toList();
    }

    public static <A extends Annotation> List<AnnotatedMethod<A>> annotatedMethods2(
            Class<?> type, Class<A> annType) {
        return methodsIn(type).stream().
                filter(m -> m.isAnnotationPresent(annType)).
                map(m -> unreflect(type, annType, m)).
                toList();
    }

    public static <A extends Annotation> List<AnnotatedMethod<A>> annotatedMethodsInSelf(
            Class<?> type, Class<A> annType) {
        return Arrays.stream(type.getDeclaredMethods()).
                filter(m -> m.isAnnotationPresent(annType)).
                map(m -> unreflect(type, annType, m)).
                toList();
    }

    private static <A extends Annotation> @Nonnull AnnotatedMethod<A> unreflect(Class<?> type, Class<A> annType, Method m) {
        m.setAccessible(true);
        try {
            MethodHandle mh = lookup().unreflect(m);
            return new AnnotatedMethod<>(
                    mh.asType(mh.type().changeParameterType(0, type)),
                    m,
                    m.getAnnotation(annType)
            );
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodType typeOf(Executable methodOrConstructor) {
        return switch (methodOrConstructor) {
            case Method m -> MethodType.methodType(m.getReturnType(), m.getParameterTypes());
            case Constructor<?> c -> MethodType.methodType(void.class, c.getParameterTypes());
        };
    }

    private static final Object ZERO_INT = 0, ZERO_DOUBLE = 0.0, ZERO_LONG = 0L, ZERO_FLOAT = 0F,
            ZERO_BYTE = (byte) 0, ZERO_SHORT = (short) 0, ZERO_CHAR = '\0';

    public static Object defaultValue(Class<?> type) {
        if (type.isPrimitive())
            // próbáltam eltalálni a UI kódok mezőtípusában szereplő gyakoriságukat
            // (referencián meg booleanen meg inten kívül nem nagyon van más),
            // talán így gyorsabb mint getName().charAt(0)-ra lookuptablezni
            if (type == boolean.class)
                return Boolean.FALSE;
            else if (type == int.class)
                return ZERO_INT;
            else if (type == double.class)
                return ZERO_DOUBLE;
            else if (type == long.class)
                return ZERO_LONG;
            else if (type == float.class)
                return ZERO_FLOAT;
            else if (type == char.class)
                return ZERO_CHAR;
            else if (type == byte.class)
                return ZERO_BYTE;
            else if (type == short.class)
                return ZERO_SHORT;
            else
                throw new RuntimeException("unknown primitive type");
        else
            return null;
    }

    public record AnnotatedMethod<ANN extends Annotation>(MethodHandle mh, Method method, ANN annotation) {}

    public record AnnotatedMember<ANN extends Annotation>(MethodHandle mh, Member member, ANN annotation) {}

    /**
     * @return először a superclass, utána a subclass. Ha statikus mező van annotálva, akkor paraméterek nélküli MH-t ad
     * vissza.
     */
    @Nonnull
    public static <ANN extends Annotation> Stream<AnnotatedMember<ANN>> annotatedMethodsAndFieldGetters(
            Class<?> type, Class<ANN> annType) {
        return Stream.concat(
                // ha record, akkor csak a metódusokat nézzük, különben duplán lennének a rekordcomponentek
                type.isRecord() ? Stream.empty() :
                        fieldsIn(type).stream().
                                filter(m -> m.isAnnotationPresent(annType)).
                                map(f -> {
                                    f.setAccessible(true);
                                    MethodHandle mh;
                                    try {
                                        mh = lookup().unreflectGetter(f);
                                        if (Modifier.isStatic(f.getModifiers()))
                                            // talán 2023-as UI Template-ek miatt lett bevezetve,
                                            // hogy statikus mezőket is támogassunk (@Content mezők miatt)
                                            mh = MethodHandles.dropArguments(mh, 0, f.getDeclaringClass());
                                        else
                                            mh = mh.asType(mh.type().changeParameterType(0, type));
                                    } catch (ReflectiveOperationException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return new AnnotatedMember<>(mh, f, f.getAnnotation(annType));
                                }),

                annotatedMethods2(type, annType).stream().map(am -> new AnnotatedMember<>(am.mh, am.method, am.annotation))
        );
    }

    public static String internalName(ClassDesc classDesc) {
        if (classDesc.isClassOrInterface()) {
            String d = classDesc.descriptorString();
            return d.substring(1, d.length() - 1);
        } else if (classDesc.isArray())
            return classDesc.descriptorString();
        else
            throw new IllegalArgumentException();
    }

    /**
     * mint Class::getName
     */
    public static String name(ClassDesc classDesc) {
        return internalName(classDesc).replace('/', '.');
    }

    public static String internalName(Class<?> clazz) {
        // TODO hidden classok?
        return clazz.getName().replace('.', '/');
    }

    public static String internalPackageName(Class<?> clazz) {
        String s = clazz.getName().replace('.', '/');
        int lastSlashIndex = s.lastIndexOf('/');
        if (lastSlashIndex == -1)
            throw new IllegalArgumentException("in top level package: " + clazz.getName());
        return s.substring(0, lastSlashIndex);
    }

    public static String simpleName(Class<?> clazz) {
        String s = clazz.getSimpleName();
        if (s.isEmpty())
            return clazz.getName();
        else
            return s;
    }

    // ritka eset hogy több iteráció van, ezért nem kell SB
    @SuppressWarnings("StringConcatenationInLoop")
    public static String nameStartingFromTopLevel(Class<?> clazz) {
        String name = "";
        do {
            name = clazz.getSimpleName() + "." + name;
            clazz = clazz.getEnclosingClass();
        } while (clazz != null);

        return name.substring(0, name.length() - 1);
    }

    public static <R> Constructor<R> canonicalConstructor(Class<R> recordClass) {
        if (!recordClass.isRecord())
            throw new IllegalArgumentException();
        RecordComponent[] recordComponents = recordClass.getRecordComponents();
        Class<?>[] paramTypes = new Class[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++)
            paramTypes[i] = recordComponents[i].getType();
        try {
            return recordClass.getDeclaredConstructor(paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("should not happen (record canonical constructor not found in " +
                    recordClass.getName() + "): " + e, e);
        }
    }

    public static MethodHandle canonicalConstructorHandle(Class<?> recordClass, Lookup lookup) throws IllegalAccessException {
        if (!recordClass.isRecord())
            throw new IllegalArgumentException();
        RecordComponent[] recordComponents = recordClass.getRecordComponents();
        Class<?>[] paramTypes = new Class[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++)
            paramTypes[i] = recordComponents[i].getType();
        try {
            return lookup.findConstructor(recordClass, MethodType.methodType(void.class, paramTypes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("should not happen (record canonical constructor not found in " +
                    recordClass.getName() + "): " + e, e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> Class<? extends V> lub(Class<? extends V> a, Class<? extends V> b) {
        if (a.isAssignableFrom(b))
            return a;
        if (b.isAssignableFrom(a))
            return b;

        Collection<Class<?>> aa = new ArrayList<>(ancestorsAndThis(a));
        Collection<Class<?>> ab = ancestorsAndThis(b);
        aa.removeIf(not(ab::contains));

        return (Class<? extends V>) aa.stream().max(Comparator.comparing(ReflectionUtil::depth)).orElseThrow();
    }

    /**
     * a visszaadott lista tartalmazhat duplicateeket
     */
    // ezt lehetne CV-ben cacheelni
    public static Collection<Class<?>> ancestorsAndThis(Class<?> clazz) {
        List<Class<?>> l = new ArrayList<>();
        l.add(clazz);
        for (int i = 0; i < l.size(); i++) {
            Class<?> c = l.get(i);
            if (c == Object.class)
                continue;
            l.add(c.getSuperclass() == null ? Object.class : c.getSuperclass());
            l.addAll(List.of(c.getInterfaces()));
        }
        return l;
    }

    @SuppressWarnings("unchecked")
    public static <I> I proxy(Class<I> interfaceType, InvocationHandler invocationHandler) {
        return (I) Proxy.newProxyInstance(STACK_WALKER.getCallerClass().getClassLoader(),
                new Class[]{interfaceType}, invocationHandler);
    }

    public static int orderedAccessLevel(Member m) {
        if (Modifier.isPublic(m.getModifiers()))
            return 4;
        if (Modifier.isProtected(m.getModifiers()))
            return 3;
        if (Modifier.isPrivate(m.getModifiers()))
            return 1;
        return 2;
    }

    /*
    public static boolean overrides(Method m1, Method m2) {
        // m1 == m2 esetén mit kéne visszadni?

        if (m1.getName().equals(m2.getName()) && m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass()) &&
                m1.getReturnType().isAssignableFrom(m2.getReturnType())
                && m1.getParameterCount() == m2.getParameterCount()) {
            Class<?>[] params1 = m1.getParameterTypes();
            Class<?>[] params2 = m2.getParameterTypes();
            for (int i = 0; i < params1.length; i++) {
                if (!params2[i].isAssignableFrom(params1[i]))
                    return false;
            }
            return true;
        } else
            return false;
    }*/

    /**
     * arra jó, hogyha setAccessible(true)-t már meghívtuk előtte. mert akkor nem lehetséges, hogy IAE keletkezik.
     * <p>
     * publicLookup-ot használ
     */
    public static MethodHandle unreflect(Method m) {
        try {
            return publicLookup().unreflect(m);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // TODO mibe kéne wrappelni?
        }
    }
}
