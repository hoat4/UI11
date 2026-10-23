package ui11;

import org.jspecify.annotations.Nullable;
import org.teavm.metaprogramming.*;
import org.teavm.metaprogramming.reflect.ReflectMethod;
import ui11.provide.FromPeerRequests;
import ui11.reflectutil.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

class IVFactory {

    static {
        if (ElementAccessorFactory.isTeaVM()) {
            // vegyük rá TeaVM-et, hogy generáljon egy if láncot az isApplicable-be, hogy a default érték 0 legyen
            if (!TeaVMIVFactory.isApplicable(Dummy1.class) ||
                !TeaVMIVFactory.isApplicable(Dummy2.class) ||
                !TeaVMIVFactory.isApplicable(Dummy3.class))
                // az if nem is feltétlen kéne, de jó ha ellenőrizzük hogy működik-e, pl. TeaVM upgrade elronthatja
                throw new RuntimeException();
            // Dummy4-nek nincs hatása, csak ellenőrizzük, hogy nem lett-e minden 0
            if (TeaVMIVFactory.isApplicable(Dummy4.class))
                throw new RuntimeException();
        }
    }

    private IVFactory() {
    }

    public static boolean isApplicable(Class<?> type) {
        if (ElementAccessorFactory.isTeaVM())
            return TeaVMIVFactory.isApplicable(type);
        else
            return ReflectionIVFactory.isApplicable(type);
    }

    public static <T> @Nullable T makeUserValue(Class<T> type,
                                                ResolutionRequestCollection resolutionRequestCollection) {
        Object value;
        if (ElementAccessorFactory.isTeaVM())
            value = TeaVMIVFactory.makeUserValue(type, resolutionRequestCollection);
        else
            value = ReflectionIVFactory.makeUserValue(type, resolutionRequestCollection);
        return type.cast(value);
    }

    // TeaVM classloader hülyeség miatt publikus az osztály és minden benne amit TeaVMIVFactory elér
    public static class ReflectionIVFactory {

        public final Method m;
        public final Class<? extends ExposeRequest<?>> peerReqType;

        private ReflectionIVFactory(Method m) {
            this.m = m;
            m.setAccessible(true);
            if (m.getParameterCount() != 1 || !m.getParameterTypes()[0].isArray())
                throw new RuntimeException("@" + FromPeerRequests.class.getSimpleName() + " method " + ReflectionUtil.memberToShortString(m) + " invalid: " +
                    "parameter count is not 1 or parameter type not array");
            if (!Modifier.isStatic(m.getModifiers()))
                throw new RuntimeException("@" + FromPeerRequests.class.getSimpleName() + " method " + ReflectionUtil.memberToShortString(m) + " invalid: " +
                    "method not static");
            Class<?> peerReqType = m.getParameterTypes()[0].getComponentType();
            assert peerReqType != null;
            if (!ExposeRequest.class.isAssignableFrom(peerReqType) || peerReqType == ExposeRequest.class)
                throw new RuntimeException("@" + FromPeerRequests.class.getSimpleName() + " method " + ReflectionUtil.memberToShortString(m) + " invalid: " +
                    "component of parameter type is not subtype of " + ExposeRequest.class.getSimpleName());

            @SuppressWarnings("unchecked")
            Class<? extends ExposeRequest<?>> casted = (Class<? extends ExposeRequest<?>>) peerReqType;
            this.peerReqType = casted;
        }

        public static final ClassValue<ReflectionIVFactory> CV = new ClassValue<ReflectionIVFactory>() {
            @Override
            protected ReflectionIVFactory computeValue(Class<?> type) {
                Method m1 = null;
                for (Method m : type.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(FromPeerRequests.class)) {
                        if (m1 == null)
                            m1 = m;
                        else
                            throw new RuntimeException("Multiple methods annotated with " +
                                "@" + FromPeerRequests.class.getSimpleName() + " in " + type.getName() + ": " +
                                ReflectionUtil.memberToShortString(m1) + ", " + ReflectionUtil.memberToShortString(m));
                    }
                }
                if (m1 == null)
                    return null;
                else
                    return new ReflectionIVFactory(m1);
            }
        };

        @Nullable
        Object makeValue(ResolutionRequestCollection resolutionRequestCollection) {
            // ezt tartsuk szinkronban TeaVMIVFactory.makeUserValue-val

            List<? extends ResolutionRequest<?>> reqs = resolutionRequestCollection.byType(peerReqType);
            ExposeRequest<?>[] result = (ExposeRequest<?>[]) Array.newInstance(peerReqType, reqs.size());
            for (int i = 0; i < reqs.size(); i++)
                result[i] = peerReqType.cast(reqs.get(i).requestData);

            try {
                return m.invoke(null, (Object) result);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e); // should not happen
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException e2)
                    throw e2;
                if (e.getCause() instanceof Error e2)
                    throw e2;
                throw new RuntimeException(e); // TODO exception message
            }
        }

        public static boolean isApplicable(Class<?> type) {
            return CV.get(type) != null;
        }

        public static Object makeUserValue(Class<?> type,
                                           ResolutionRequestCollection resolutionRequestCollection) {
            return CV.get(type).makeValue(resolutionRequestCollection);
        }
    }

    @CompileTime
    private static class TeaVMIVFactory {

        @Meta
        static native boolean isApplicable(Class<?> type);

        private static void isApplicable(ReflectClass<?> type) throws ClassNotFoundException {
            Class<?> c = Class.forName(type.getName(), false,
                Metaprogramming.getClassLoader());

            if (c == Dummy1.class || c == Dummy2.class || c == Dummy3.class) {
                Metaprogramming.exit(() -> true);
                return;
            }

            if (!ReflectionIVFactory.isApplicable(c)) {
                Metaprogramming.unsupportedCase(); // remélhetőleg ilyenkor 0 lesz az eredmény
                return;
            }

            Metaprogramming.exit(() -> true);
        }

        @Meta
        static native @Nullable Object makeUserValue(Class<?> type,
                                                     ResolutionRequestCollection resolutionRequestCollection);

        private static void makeUserValue(ReflectClass<?> type,
                                          Value<ResolutionRequestCollection> resolutionRequestCollection)
            throws ClassNotFoundException {
            // ezt tartsuk szinkronban ReflectionIVFactory.makeValue-val

            Class<?> c = Class.forName(type.getName(), false, Metaprogramming.getClassLoader());
            if (!ReflectionIVFactory.isApplicable(c)) {
                Metaprogramming.unsupportedCase();
                return;
            }

            ReflectionIVFactory r = ReflectionIVFactory.CV.get(c);
            ReflectMethod m = TeaVMMetaprogrammingUtil.findMethod(r.m);
            ReflectClass<? extends ExposeRequest<?>> peerReqTypeTeaVM = Metaprogramming.findClass(r.peerReqType);
            Metaprogramming.exit(() -> {
                Class<? extends ExposeRequest<?>> peerReqType = peerReqTypeTeaVM.asJavaClass();
                List<? extends ResolutionRequest<?>> reqs = resolutionRequestCollection.get().byType(peerReqType);
                ExposeRequest<?>[] filteredReqsLocal = (ExposeRequest<?>[]) Array.newInstance(peerReqType, reqs.size());
                for (int i = 0; i < reqs.size(); i++)
                    filteredReqsLocal[i] = peerReqType.cast(reqs.get(i).requestData);

                Object result;
                try {
                    result = m.invoke(null, (Object) filteredReqsLocal);
                } catch (Throwable e) {
                    if (e.getCause() instanceof RuntimeException e2)
                        throw e2;
                    if (e.getCause() instanceof Error e2)
                        throw e2;
                    throw new RuntimeException(e); // TODO exception message
                }
                return result;
            });
        }
    }

    // ld. komment fent clinitben
    public static class Dummy1 {}

    public static class Dummy2 {}

    public static class Dummy3 {}

    private static class Dummy4 {}
}
