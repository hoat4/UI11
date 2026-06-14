package ui.platform.glass.windows;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.structLayout;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.invoke.MethodType.methodType;

@SuppressWarnings("preview")
public class NativeStructMapper<S> {

    private final NativeStructInfo struct;
    private final MethodHandle parser; // (MemorySegment)Object

    private final ValueLayout.OfInt cbSizeLayout;
    private final long cbSizeOffset;

    public NativeStructMapper(Class<S> structClass, MethodHandles.Lookup lookup) {
        struct = new NativeStructInfo(structClass, lookup);
        parser = struct.makeParser(struct.layout, new ArrayList<>()).asType(methodType(Object.class, MemorySegment.class));

        if (struct.prefixWithStructSize) {
            cbSizeLayout = (ValueLayout.OfInt) struct.layout.select(groupElement("cbSize"));
            cbSizeOffset = struct.layout.byteOffset(groupElement("cbSize"));
        } else {
            cbSizeLayout = null;
            cbSizeOffset = 0;
        }
    }

    public MemorySegment allocate(Arena arena) {
        MemorySegment memorySegment = arena.allocate(struct.layout);
        if (cbSizeLayout != null)
            memorySegment.set(cbSizeLayout, cbSizeOffset, Math.toIntExact(struct.layout.byteSize()));
        return memorySegment;
    }

    @SuppressWarnings("unchecked")
    public S parse(MemorySegment memorySegment) {
        try {
            return (S) parser.invokeExact(memorySegment);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Target(TYPE)
    @Retention(RUNTIME)
    @Documented
    public @interface Struct {
        boolean prefixWithStructSize() default false;

        boolean packed();
    }

    private static class NativeStructInfo {

        private final Class<?> structClass;
        private final boolean prefixWithStructSize;
        private final String[] fieldNames;
        private final Object[] fieldTypes;
        private final StructLayout layout;
        private final MethodHandle canonicalConstructor;

        public NativeStructInfo(Class<?> structClass, MethodHandles.Lookup lookup) {
            Struct ann;
            if (!structClass.isRecord() || (ann = structClass.getAnnotation(Struct.class)) == null)
                throw new IllegalArgumentException();
            this.structClass = structClass;
            this.prefixWithStructSize = ann.prefixWithStructSize();
            boolean packed = ann.packed();

            if (!packed)
                throw new RuntimeException("TODO");

            RecordComponent[] recordComponents = structClass.getRecordComponents();
            fieldNames = new String[recordComponents.length];
            fieldTypes = new Object[recordComponents.length];
            Class<?>[] canonicalConstructorParamTypes = new Class[recordComponents.length];
            MemoryLayout[] fieldLayouts = new MemoryLayout[(prefixWithStructSize ? 1 : 0) + fieldNames.length];
            int i = 0;
            if (prefixWithStructSize) {
                fieldLayouts[i++] = JAVA_INT.withName("cbSize");
            }
            for (int j = 0; j < recordComponents.length; j++) {
                RecordComponent recordComponent = recordComponents[j];
                Class<?> type = recordComponent.getType();
                canonicalConstructorParamTypes[j] = type;
                Object t;
                MemoryLayout l;
                if (type == int.class) {
                    t = type;
                    l = JAVA_INT;
                    if (packed)
                        l = l.withByteAlignment(1);
                } else if (type == long.class) {
                    t = type;
                    l = JAVA_LONG;
                    if (packed)
                        l = l.withByteAlignment(1);
                } else {
                    NativeStructInfo s = new NativeStructInfo(type, lookup);
                    if (s.prefixWithStructSize)
                        throw new RuntimeException("TODO");
                    if (packed && s.layout.byteAlignment() != 1)
                        // TODO ilyenkor mit csinál egy C fordító?
                        throw new RuntimeException("outer struct packed, inner struct non packed");
                    t = s;
                    l = s.layout;
                }
                fieldTypes[j] = t;
                String name = recordComponent.getName();
                fieldNames[j] = name;
                fieldLayouts[i++] = l.withName(name);
            }
            layout = structLayout(fieldLayouts);
            try {
                canonicalConstructor = lookup.findConstructor(structClass,
                        methodType(void.class, canonicalConstructorParamTypes));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e); // TODO
            }
        }

        public MethodHandle makeParser(MemoryLayout root, List<MemoryLayout.PathElement> stack) {
            MethodHandle[] fieldParsers = new MethodHandle[fieldNames.length];
            for (int i = 0; i < fieldParsers.length; i++) {
                stack.add(MemoryLayout.PathElement.groupElement(fieldNames[i]));
                Object t = fieldTypes[i];
                if (t == int.class || t == long.class) {
                    fieldParsers[i] = root.varHandle(stack.toArray(MemoryLayout.PathElement[]::new)).
                            toMethodHandle(VarHandle.AccessMode.GET);
                } else {
                    fieldParsers[i] = ((NativeStructInfo) t).makeParser(root, stack);
                }
                stack.removeLast();
            }
            MethodHandle mh = MethodHandles.filterArguments(canonicalConstructor, 0, fieldParsers);
            mh = MethodHandles.permuteArguments(mh,
                    methodType(structClass, MemorySegment.class), new int[fieldNames.length]);
            return mh;
        }
    }
}
