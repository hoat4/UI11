package ui11;


@org.teavm.metaprogramming.CompileTime
class ElementAccessorFactory {

    @SuppressWarnings("unchecked")
    public static <T extends Widget> WidgetAccessor<T> accessorFor(Class<T> c) {
        if (isTeaVM())
            return TeaVMWidgetAccessor.accessorFor(c);
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
            WidgetDefinitionParser reflector = new WidgetDefinitionParser(
                    type.asSubclass(Widget.class));
            reflector.reflect();
            return new RegularWidgetAccessor<>(reflector);
        }
    };
}
