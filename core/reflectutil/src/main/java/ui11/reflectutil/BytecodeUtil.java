package ui11.reflectutil;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.constant.ClassDesc;
import java.util.Arrays;

import static org.objectweb.asm.Opcodes.*;

public class BytecodeUtil {

    private BytecodeUtil() {
        throw new Error();
    }

    public static void unbox(Class<?> propType, MethodVisitor mv) {
        if (propType == int.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        } else if (propType == double.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
        } else if (propType == boolean.class) {
            mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        } else
            throw new RuntimeException("TODO");
    }

    /**
     * a verem tetején Object van
     */
    public static void unboxOrCast(Class<?> expectedType, MethodVisitor mv) {
        if (expectedType.isPrimitive())
            unbox(expectedType, mv);
        else
            mv.visitTypeInsn(CHECKCAST, ReflectionUtil.internalName(expectedType));
    }

    public static void box(Class<?> propType, MethodVisitor mv) {
        if (propType == int.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
                    "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (propType == double.class)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double",
                    "valueOf", "(D)Ljava/lang/Double;", false);
        else if (propType == boolean.class)
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean",
                    "valueOf", "(Z)Ljava/lang/Boolean;", false);
        else
            throw new RuntimeException("TODO " + propType.getName());
    }

    public static void box(ClassDesc primitiveType, MethodVisitor mv) {
        switch (primitiveType.descriptorString()) {
            case "I" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
                    "valueOf", "(I)Ljava/lang/Integer;", false);
            case "D" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double",
                    "valueOf", "(D)Ljava/lang/Double;", false);
            case "Z" -> mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean",
                    "valueOf", "(Z)Ljava/lang/Boolean;", false);
            default -> throw new RuntimeException("TODO " + primitiveType);
        }
    }

    public static void boxOrUnboxIfPrimitive(MethodVisitor mv, ClassDesc prev, ClassDesc next) {
        if (prev.equals(next))
            return;
        if (prev.isPrimitive()) {
            switch (prev.descriptorString()) {
                case "I" -> {
                    expectType(next, "Ljava/lang/Integer;");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer",
                            "valueOf", "(I)Ljava/lang/Integer;", false);
                }
                case "D" -> {
                    expectType(next, "Ljava/lang/Double;");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double",
                            "valueOf", "(D)Ljava/lang/Double;", false);
                }
                case "Z" -> {
                    expectType(next, "Ljava/lang/Boolean;");
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean",
                            "valueOf", "(Z)Ljava/lang/Boolean;", false);
                }
                default -> {
                    throw new RuntimeException("TODO " + prev);
                }
            }
        } else if (next.isPrimitive()) {
            switch (next.descriptorString()) {
                case "I" -> {
                    expectType(prev, "Ljava/lang/Integer;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer",
                            "intValue", "()I", false);
                }
                case "D" -> {
                    expectType(prev, "Ljava/lang/Double;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double",
                            "doubleValue", "()D", false);
                }
                case "Z" -> {
                    expectType(prev, "Ljava/lang/Boolean;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean",
                            "booleanValue", "()Z", false);
                }
                default -> {
                    throw new RuntimeException("TODO " + prev);
                }
            }
        }
    }

    private static void expectType(ClassDesc actual, String expected) {
        if (!actual.descriptorString().equals(expected))
            throw new IllegalArgumentException("Expected " + expected + ", but got " + actual);
    }

    public static void emitIntConstant(MethodVisitor mv, int v) {
        if (ICONST_M1 - ICONST_0 <= v && v <= ICONST_5 - ICONST_0)
            mv.visitInsn(ICONST_0 + v);
        else if (v == (byte) v)
            mv.visitIntInsn(BIPUSH, v);
        else if (v == (short) v)
            mv.visitIntInsn(SIPUSH, v);
        else
            mv.visitLdcInsn(v);
    }

    public static int slots(Class<?> type) {
        if (type == void.class)
            throw new IllegalArgumentException();
        return type == double.class || type == long.class ? 2 : 1;
    }

    private static final byte[] bytecodeOffsets = new byte[64];
    private static final byte[] arrayBytecodeOffsets = new byte[64];

    static {
        Arrays.fill(bytecodeOffsets, (byte) -1);

        bytecodeOffsets['B' - 64] = 0;
        bytecodeOffsets['Z' - 64] = 0;
        bytecodeOffsets['S' - 64] = 0;
        bytecodeOffsets['C' - 64] = 0;
        bytecodeOffsets['I' - 64] = 0;
        bytecodeOffsets['J' - 64] = 1;
        bytecodeOffsets['F' - 64] = 2;
        bytecodeOffsets['D' - 64] = 3;
        bytecodeOffsets['L' - 64] = 4;

        // Class.getName
        bytecodeOffsets['b' - 64] = 0; // byte, boolean
        bytecodeOffsets['s' - 64] = 0; // short
        bytecodeOffsets['c' - 64] = 0; // char
        bytecodeOffsets['i' - 64] = 0; // int
        bytecodeOffsets['l' - 64] = 1; // long
        bytecodeOffsets['f' - 64] = 2; // float
        bytecodeOffsets['d' - 64] = 3; // double

        Arrays.fill(arrayBytecodeOffsets, (byte) -1);
        arrayBytecodeOffsets['b' - 64] = BASTORE - IASTORE; // byte, boolean
        arrayBytecodeOffsets['s' - 64] = SASTORE - IASTORE; // short
        arrayBytecodeOffsets['c' - 64] = CASTORE - IASTORE; // char
        arrayBytecodeOffsets['i' - 64] = 0; // int
        arrayBytecodeOffsets['l' - 64] = LASTORE - IASTORE; // long
        arrayBytecodeOffsets['f' - 64] = FASTORE - IASTORE; // float
        arrayBytecodeOffsets['d' - 64] = DASTORE - IASTORE; // double
    }

    public static int bytecodeOffset(Class<?> type) {
        if (type.isPrimitive()) {
            byte bytecodeOffset = bytecodeOffsets[type.getName().charAt(0) - 64];
            assert bytecodeOffset != -1;
            return bytecodeOffset;
        } else
            return 4;
    }

    public static int bytecodeOffset(String type) {
        int offset = bytecodeOffsets[type.charAt(0) - 64];
        if (offset == -1)
            throw new IllegalArgumentException();
        return offset;
    }

    public static int arrayLoadStoreInsnOffset(Class<?> type) {
        if (type.isPrimitive()) {
            byte bytecodeOffset = arrayBytecodeOffsets[type.getName().charAt(0) - 64];
            assert bytecodeOffset != -1;
            return bytecodeOffset;
        } else
            return AASTORE - IASTORE;
    }
}
