package ui11;

import org.teavm.backend.javascript.codegen.SourceWriter;

// functions that work with TeaVM Metaprogramming API-hoz doesn't belong here, instead in TeaVMMetaprogrammingUtil
class TeaVMUtil {

    private TeaVMUtil() {}

    /**
     * difference between this and {@link SourceWriter#appendClass(String)} is that this
     * can handle array types
     */
    static void appendClass(Class<?> c, SourceWriter out) {
        int arrayDimension = 0;
        while (c.isArray()) {
            out.appendFunction("$rt_arraycls");
            out.append('(');
            c = c.getComponentType();
            arrayDimension++;
        }
        out.appendClass(c);
        for (int i = 0; i < arrayDimension; i++)
            out.append(')');
    }
}
