package ui11;

import ui11.reflectutil.ReflectionUtil;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.teavm.metaprogramming.*;
import org.teavm.metaprogramming.impl.reflect.ReflectClassImpl;
import org.teavm.metaprogramming.reflect.ReflectMethod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.stream;
import static org.objectweb.asm.Opcodes.*;

@CompileTime
class ListenerProxyGenerator {

    private static final AtomicInteger INTERFACE_NAME_COUNTER = new AtomicInteger();
    private static final String Element_originalModel_FIELD_NAME = "model";

    private final ClassWriter proxyClassWriter;
    private final String proxyName;

    private static final String baseClassName =
            ReflectionUtil.internalName(ListenerProxyGenerator.class).substring(0,
                    ReflectionUtil.internalName(ListenerProxyGenerator.class).lastIndexOf('/') + 1) +
                    "ListenerProxyBase";

    private final String widgetRecordType;
    private final String widgetRecordFieldName;
    private final String widgetRecordFieldAccessorType;

    /**
     *
     * @param superinterfaceNames internal names
     */
    ListenerProxyGenerator(String[] superinterfaceNames, RecordComponent recordComponent) {
        this.widgetRecordType = ReflectionUtil.internalName(recordComponent.getDeclaringRecord());
        this.widgetRecordFieldName = recordComponent.getName();
        this.widgetRecordFieldAccessorType = "()" + recordComponent.getType().descriptorString();

        proxyName = ReflectionUtil.internalName(ListenerProxyGenerator.class) + "$$" +
                "GeneratedListenerProxy" + INTERFACE_NAME_COUNTER.incrementAndGet();

        proxyClassWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        proxyClassWriter.visit(V21, 0,
                proxyName,
                null,
                baseClassName,
                superinterfaceNames);

        String constructorDesc = "(" + ElementDefReflector.ELEMENT_CLASS.descriptorString() + ")V";
        MethodVisitor constructor = proxyClassWriter.visitMethod(0, "<init>",
                constructorDesc, null, null);
        // eggyel több paraméter, mert inner class
        constructor.visitVarInsn(ALOAD, 0);
        constructor.visitVarInsn(ALOAD, 1);
        constructor.visitMethodInsn(INVOKESPECIAL, baseClassName,
                "<init>", constructorDesc, false);
        constructor.visitInsn(RETURN);
        constructor.visitMaxs(0, 0);
        constructor.visitEnd();
    }

    byte[] finish() {
        return proxyClassWriter.toByteArray();
    }

    /**
     *
     * @return {@code (LElement;)Ljava/lang/Object;}
     */
    // legyen konzisztens makeProxy_teavm-mel
    static MethodHandle makeProxyFactory(RecordComponent recordComponent) {
        // ElementDefReflector ellenőrizte már, hogy interface-e
        Class<?>[] interfaces = {recordComponent.getType()};

        // TODO kezelni kéne azt is, ha két eltérő interface van a hierarchiában, de ugyanazon néven
        //      (különbözik a classloaderük)
        String[] superinterfaceNames = new String[interfaces.length];
        for (int i = 0; i < superinterfaceNames.length; i++)
            superinterfaceNames[i] = ReflectionUtil.internalName(interfaces[i]);

        ListenerProxyGenerator g = new ListenerProxyGenerator(superinterfaceNames, recordComponent);
        List<Class<?>> q = new ArrayList<>(Arrays.asList(interfaces));
        for (int i = 0; i < q.size(); i++) {
            Class<?> iface = q.get(i);
            final String ifaceInternalName = iface.getName().replace('.', '/');
            for (Method m : iface.getDeclaredMethods()) {
                Class<?>[] parameterTypes = m.getParameterTypes();
                if (g.shouldProcessMethod(m.getModifiers(), m.getName(), parameterTypes.length)) {
                    String desc = "(" + stream(parameterTypes).map(Class::descriptorString).
                            collect(Collectors.joining()) + ")" + m.getReturnType().descriptorString();
                    g.processMethod(ifaceInternalName, m.getName(), desc);
                }
            }
            for (Class<?> iface2 : iface.getInterfaces()) {
                if (!q.contains(iface2))
                    q.add(iface2);
            }
        }

        byte[] classfile = g.finish();
        Class<?> c;
        Lookup lookup = MethodHandles.lookup();
        MethodHandle constructor;
        try {
            c = lookup.defineClass(classfile);
            constructor = lookup.findConstructor(c, methodType(void.class, Element.class));
        } catch (IllegalAccessException | NoSuchMethodException e) {
            // TODO
            throw new RuntimeException(e);
        }

        constructor = constructor.asType(methodType(Object.class, Element.class));

        return constructor;
    }

    @SuppressWarnings("RedundantIfStatement")
    boolean shouldProcessMethod(int modifiers, String name, int parameterCount) {
        if (Modifier.isStatic(modifiers))
            return false;

        if (name.equals("toString") && parameterCount == 0 ||
                name.equals("hashCode") && parameterCount == 0 ||
                name.equals("equals") && parameterCount == 1)
            return false;

        return true;
    }

    void processMethod(String owner, String name, String desc) {
        MethodVisitor mv = proxyClassWriter.visitMethod(ACC_PUBLIC, name, desc,
                null, null);
        int returnOpcode = Type.getReturnType(desc).getOpcode(IRETURN);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, baseClassName, "element", ElementDefReflector.ELEMENT_CLASS.descriptorString());
        mv.visitFieldInsn(GETFIELD, ReflectionUtil.internalName(ElementDefReflector.ELEMENT_CLASS), Element_originalModel_FIELD_NAME,
                Object.class.descriptorString());
        mv.visitTypeInsn(CHECKCAST, widgetRecordType);
        mv.visitMethodInsn(INVOKEVIRTUAL, widgetRecordType, widgetRecordFieldName,
                widgetRecordFieldAccessorType, false);
        mv.visitTypeInsn(CHECKCAST, owner);
        int var = 1;
        for (Type paramType : Type.getArgumentTypes(desc)) {
            mv.visitVarInsn(paramType.getOpcode(ILOAD), var);
            var += paramType.getSize();
        }
        mv.visitMethodInsn(INVOKEINTERFACE, owner,
                name, desc, true);
        mv.visitInsn(returnOpcode);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
}

@CompileTime
class ListenerProxyGeneratorTeaVMSupport {

    ListenerProxyGeneratorTeaVMSupport() {
        throw new Error();
    }

    // legyen konzisztens makeProxy-val
    @SuppressWarnings("Convert2MethodRef")
    static Value<?> makeProxy_teavm(Value<Element> e, RecordComponent recordComponent) {
        List<ReflectClass<?>> superinterfaces = new ArrayList<>();
        // ElementDefReflector ellenőrizte már, hogy interface-e
        superinterfaces.add(Metaprogramming.findClass(recordComponent.getType()));

        String[] superinterfaceNames = superinterfaces.stream().
                map(c -> c.getName().replace('.', '/')).
                toArray(String[]::new);

        ListenerProxyGenerator g = new ListenerProxyGenerator(superinterfaceNames, recordComponent);

        List<ReflectClass<?>> q = new ArrayList<>(superinterfaces);
        for (int i = 0; i < q.size(); i++) {
            ReflectClass<?> iface = q.get(i);
            final String ifaceInternalName = iface.getName().replace('.', '/');
            for (ReflectMethod m : iface.getDeclaredMethods()) {
                final ReflectClass<?>[] parameterTypes = m.getParameterTypes();
                if (g.shouldProcessMethod(m.getModifiers(), m.getName(), parameterTypes.length)) {
                    String desc = "(" + stream(parameterTypes).map(ptype -> teavmClassToType(ptype)).
                            collect(Collectors.joining()) + ")" + teavmClassToType(m.getReturnType());
                    g.processMethod(ifaceInternalName, m.getName(), desc);
                }
            }
            for (ReflectClass<?> iface2 : iface.getInterfaces()) {
                if (!q.contains(iface2))
                    q.add(iface2);
            }
        }

        ReflectClass proxyClass = Metaprogramming.createClass(g.finish());

        int interfaceCount = superinterfaceNames.length;
        Value<Class<?>[]> interfacesArrayRT = Metaprogramming.emit(() -> new Class<?>[interfaceCount]);
        for (int i = 0; i < interfaceCount; i++) {
            int i0 = i;
            ReflectClass<?> superinterface0 = superinterfaces.get(i);
            Metaprogramming.emit(() -> interfacesArrayRT.get()[i0] = superinterface0.asJavaClass());
        }
        ReflectMethod reflectConstructor = proxyClass.getDeclaredJMethod("<init>", ElementDefReflector.ELEMENT_CLASS);
        Objects.requireNonNull(reflectConstructor);
        return Metaprogramming.emit(() -> reflectConstructor.construct(e.get()));
    }

    private static String teavmClassToType(ReflectClass<?> clazz) {
        // nem értem, minek rejtegetik annyira
        return ((ReflectClassImpl<?>) clazz).type.toString();
    }
}
