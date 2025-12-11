package ui11;


@org.teavm.metaprogramming.CompileTime
class ElementAccessorFactory {

    @SuppressWarnings("unchecked")
    public static <T extends Widget> WidgetAccessor<T> accessorFor(Class<T> c) {
        if (isTeaVM())
            return TeaVMElementAccessorFactory.getElementAccessor_TeaVM(c);
        else
            return (WidgetAccessor<T>) CV.get(c);
    }

    @org.teavm.interop.PlatformMarker
    static boolean isTeaVM() {
        return false;
    }

    // TODO ha egyszer exception dobódott CF.get-ben, akkor másodszorra már nem kéne kiírni
    private static final ClassValue<WidgetAccessor<?>> CV = new ClassValue<>() {
        @Override
        protected WidgetAccessor<?> computeValue(Class<?> type) {
            ElementDefReflector reflector = new ElementDefReflector(
                    type.asSubclass(Widget.class), false);
            reflector.reflect();
            return new RegularWidgetAccessor<>(reflector);
        }
    };
}
