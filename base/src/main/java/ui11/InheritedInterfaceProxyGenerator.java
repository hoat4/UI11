package ui11;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import ui11.WidgetState.InheritedPropBase;
import ui11.reflectutil.ReflectionUtil;

import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DirectMethodHandleDesc.Kind;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.stream;
import static org.objectweb.asm.Opcodes.*;

class InheritedInterfaceProxyGenerator {

    private static final AtomicInteger INTERFACE_NAME_COUNTER = new AtomicInteger();
    private static final String BASE_CLASS_NAME = ReflectionUtil.internalName(InheritedInterfaceProxyBase.class);
    private static final String GET_DELEGATE_METHOD_NAME = "getDelegate";
    private static final String GET_DELEGATE_METHOD_DESC = methodType(Object.class,
            InheritedInterfaceProxyBase.Dummy.class).descriptorString();
    private static final String CONSTRUCTOR_DESC =
            methodType(void.class, WidgetState.class, Class.class, boolean.class, String.class).descriptorString();
    private static final String FACTORY_METHOD_NAME = "create";
    private static final String TOSTRING_PREFIX = "Inherited value interface proxy for ";

    private final ClassWriter proxyClassWriter;
    private final String proxyClassName;
    private final String interfaceName;
    private final List<DirectMethodHandleDesc> processedMethods = new ArrayList<>();

    InheritedInterfaceProxyGenerator(Class<?> interfaceType) {
        this.interfaceName = ReflectionUtil.internalName(interfaceType);

        proxyClassName = ReflectionUtil.internalName(InheritedInterfaceProxyGenerator.class) + "$$" +
                "GeneratedProxy" + INTERFACE_NAME_COUNTER.incrementAndGet();

        proxyClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        proxyClassWriter.visit(V21, 0,
                proxyClassName,
                null,
                BASE_CLASS_NAME,
                new String[]{interfaceName});

        MethodVisitor constructor = proxyClassWriter.visitMethod(0, "<init>",
                CONSTRUCTOR_DESC, null, null);
        constructor.visitVarInsn(ALOAD, 0); // this
        constructor.visitVarInsn(ALOAD, 1);
        constructor.visitVarInsn(ALOAD, 2);
        constructor.visitVarInsn(ILOAD, 3);
        constructor.visitMethodInsn(INVOKESPECIAL, BASE_CLASS_NAME, "<init>", CONSTRUCTOR_DESC, false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();

        MethodVisitor factoryMethod = proxyClassWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, FACTORY_METHOD_NAME,
                factoryMethod().lookupDescriptor(), null, null);
        factoryMethod.visitTypeInsn(NEW, proxyClassName);
        factoryMethod.visitInsn(DUP);
        factoryMethod.visitVarInsn(ALOAD, 0);
        factoryMethod.visitVarInsn(ALOAD, 1);
        factoryMethod.visitVarInsn(ILOAD, 2);
        factoryMethod.visitVarInsn(ALOAD, 3);
        factoryMethod.visitMethodInsn(INVOKESPECIAL, proxyClassName, "<init>", CONSTRUCTOR_DESC, false);
        factoryMethod.visitInsn(ARETURN);
        factoryMethod.visitMaxs(0, 0);
        factoryMethod.visitEnd();

        List<Class<?>> q = new ArrayList<>();
        q.add(interfaceType);
        for (int i = 0; i < q.size(); i++) {
            Class<?> iface = q.get(i);
            String ifaceInternalName = ReflectionUtil.internalName(iface);
            for (Method m : iface.getDeclaredMethods()) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (shouldProcessMethod(m.getModifiers(), m.getName(), parameterTypes.length)) {
                    String desc = "(" + stream(parameterTypes).map(Class::descriptorString).
                            collect(Collectors.joining()) + ")" + m.getReturnType().descriptorString();
                    processMethod(ifaceInternalName, m.getName(), desc, false);
                }
            }
            for (Class<?> iface2 : iface.getInterfaces()) {
                if (!q.contains(iface2))
                    q.add(iface2);
            }
        }

        processMethod(interfaceName, "toString", "()Ljava/lang/String;", true);
    }

    @SuppressWarnings("RedundantIfStatement")
    private boolean shouldProcessMethod(int modifiers, String name, int parameterCount) {
        if (Modifier.isStatic(modifiers))
            return false;

        if (name.equals("toString") && parameterCount == 0 ||
                name.equals("hashCode") && parameterCount == 0 ||
                name.equals("equals") && parameterCount == 1)
            return false;

        return true;
    }

    private void processMethod(String owner, String name, String desc, boolean isToString) {
        processedMethods.add(MethodHandleDesc.ofMethod(Kind.INTERFACE_VIRTUAL,
                ClassDesc.ofInternalName(owner), name, MethodTypeDesc.ofDescriptor(desc)));

        MethodVisitor mv = proxyClassWriter.visitMethod(ACC_PUBLIC, name, desc,
                null, null);
        if (isToString)
            mv.visitLdcInsn(TOSTRING_PREFIX);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ACONST_NULL); // dummy arg
        mv.visitMethodInsn(INVOKESPECIAL, BASE_CLASS_NAME, GET_DELEGATE_METHOD_NAME,
                GET_DELEGATE_METHOD_DESC, false);
        mv.visitTypeInsn(CHECKCAST, interfaceName);
        int var = 1;
        for (Type paramType : Type.getArgumentTypes(desc)) {
            mv.visitVarInsn(paramType.getOpcode(ILOAD), var);
            var += paramType.getSize();
        }
        mv.visitMethodInsn(INVOKEINTERFACE, owner,
                name, desc, true);
        if (isToString)
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat",
                    "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(Type.getReturnType(desc).getOpcode(IRETURN));
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    DirectMethodHandleDesc factoryMethod() {
        return MethodHandleDesc.ofMethod(Kind.STATIC, ClassDesc.ofInternalName(proxyClassName),
                FACTORY_METHOD_NAME,
                MethodTypeDesc.of(
                        ClassDesc.ofInternalName(interfaceName),
                        WidgetState.class.describeConstable().get(),
                        Class.class.describeConstable().get(),
                        boolean.class.describeConstable().get(),
                        String.class.describeConstable().get()
                )
        );
    }

    List<DirectMethodHandleDesc> processedMethods() {
        return Collections.unmodifiableList(processedMethods);
    }

    byte[] toClassfile() {
        return proxyClassWriter.toByteArray();
    }

    static class InheritedInterfaceProxyBase<T> extends InheritedPropBase<T> {

        public InheritedInterfaceProxyBase(WidgetState<?> stateWidget, Class<T> type, boolean optional,
                                           String fieldDebugName) {
            super(stateWidget, type, optional, fieldDebugName);
        }

        protected final Object getDelegate(Dummy dummy) {
            return super.get();
        }

        // hogy ne ütközzön getDelegate method nameandtype-ja az interface egy metódusával
        static class Dummy {}
    }
}
