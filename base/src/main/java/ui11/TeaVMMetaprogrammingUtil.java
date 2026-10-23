package ui11;

import org.jspecify.annotations.NonNull;
import org.teavm.metaprogramming.CompileTime;
import org.teavm.metaprogramming.Metaprogramming;
import org.teavm.metaprogramming.ReflectClass;
import org.teavm.metaprogramming.Value;
import org.teavm.metaprogramming.reflect.ReflectMethod;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

@CompileTime
class TeaVMMetaprogrammingUtil {

    public static ReflectMethod findMethod(Executable method) {
        ReflectClass<?> c1 = Metaprogramming.findClass(method.getDeclaringClass());

        return findMethodIn(method, c1);
    }

    public static @NonNull ReflectMethod findMethodIn(Executable method, ReflectClass<?> c1) {
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

    private static @NonNull Value<?> zero(Class<?> type) {
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

    private static final Byte ZERO_BYTE = (byte) 0;
    private static final Short ZERO_SHORT = (short) 0;
    private static final Character ZERO_CHAR = '\0';
    private static final Integer ZERO_INT = 0;
    private static final Float ZERO_FLOAT = 0F;
    private static final Long ZERO_LONG = 0L;
    private static final Double ZERO_DOUBLE = 0D;

    private static @NonNull Value<Object[]> objArray(Object[] values) {
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
}
