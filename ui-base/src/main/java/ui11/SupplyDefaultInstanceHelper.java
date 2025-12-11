package ui11;

import ui11.provide.SupplyDefaultInstance;
import org.teavm.interop.PlatformMarker;
import org.teavm.metaprogramming.Meta;
import org.teavm.metaprogramming.Metaprogramming;
import org.teavm.metaprogramming.ReflectClass;
import org.teavm.metaprogramming.reflect.ReflectMethod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import static java.lang.invoke.MethodHandles.lookup;
import static java.lang.invoke.MethodHandles.privateLookupIn;

class SupplyDefaultInstanceHelper {

    private SupplyDefaultInstanceHelper() {
        throw new Error();
    }

    static {
        if (ElementAccessorFactory.isTeaVM()) {
            // Ha csak egy lehetőség van egy @Meta függvényben és a többi unsupportedCase,
            // akkor csak annak az egynek a kódját generálja oda a TeaVM, ellenőrzések nélkül.
            // Ezért belerakunk kettőt, hogy egynél biztosan több lesz benne.
            // Így valami bonyolult switch majd if-else-re fordul le, aminek a végén 0-ra állítja be a return value-t,
            // ha egyik se teljesült.
            @SupplyDefaultInstance
            class A {}
            @SupplyDefaultInstance
            class B {}

            shouldUseDefaultInstance(A.class);
            shouldUseDefaultInstance(B.class);
        }
    }

    static boolean shouldUseDefaultInstance(Class<?> type) {
        if (ElementAccessorFactory.isTeaVM()) {
            return TeaVMSupport.shouldUseDefaultInstance(type);
        } else {
            return CV.get(type) != null;
        }
    }

    static Object createDefaultInstance(Class<?> type) {
        if (ElementAccessorFactory.isTeaVM()) {
            return TeaVMSupport.createDefaultInstance(type);
        } else {
            MethodHandle mh = CV.get(type);
            try {
                return mh.invokeExact();
            } catch (Error | RuntimeException e) {
                throw e;
            } catch (Throwable e) {
                throw new RuntimeException("Can't create instance of " + type.getName() + ": " + e, e);
            }
        }
    }

    private static final ClassValue<MethodHandle> CV = new ClassValue<MethodHandle>() {
        @Override
        protected MethodHandle computeValue(Class<?> type) {
            if (type.isAnnotationPresent(SupplyDefaultInstance.class)) {
                try {
                    return privateLookupIn(type, lookup()).
                            findConstructor(type, MethodType.methodType(void.class)).
                            asType(MethodType.methodType(Object.class));
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    throw new RuntimeException("Can't create instance of " + type.getName() + ": " + e, e);
                }
            } else {
                return null;
            }
        }
    };

    @org.teavm.metaprogramming.CompileTime
    private static class TeaVMSupport {

        @Meta
        public static native boolean shouldUseDefaultInstance(Class<?> type);

        @Meta
        public static native Object createDefaultInstance(Class<?> type);

        private static void shouldUseDefaultInstance(ReflectClass<?> type) {
            if (type.getAnnotation(SupplyDefaultInstance.class) != null)
                Metaprogramming.exit(() -> true);
            else
                Metaprogramming.unsupportedCase(); // TODO ezt false-nak fogja érzékelni?
        }

        @SuppressWarnings("DataFlowIssue")
        private static void createDefaultInstance(ReflectClass<?> type) {
            if (type.getAnnotation(SupplyDefaultInstance.class) != null) {
                ReflectMethod constructor = type.getDeclaredMethod("<init>");
                if (constructor == null)
                    throw new RuntimeException("Can't create instance of " + type.getName() + ", " +
                            "because no zero-parameter constructor exists in it");
                Metaprogramming.exit(() -> constructor.construct());
            } else
                Metaprogramming.unsupportedCase(); // TODO ezt false-nak fogja érzékelni?
        }
    }
}
