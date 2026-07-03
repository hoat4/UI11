package ui11;

abstract class Component extends Widget {

    protected abstract void update();

    @Override
    protected final Widget build() {
        update();
        return ComponentResult.INSTANCE;
    }

    protected void useComponent(Slot slot, Widget component) {
        useWidget(slot, component, ComponentResultRequest.instance());
    }

    private static class ComponentResult extends SubstitutedWidget {
        static final ComponentResult INSTANCE = new ComponentResult();
    }

    static class ComponentResultRequest extends PeerCreationRequest<ComponentResult> {

        private static final ComponentResultRequest INSTANCE = new ComponentResultRequest();

        private ComponentResultRequest() {
            super(ComponentResult.class);
        }

        public static <R> ComponentResultRequest instance() {
            return INSTANCE;
        }
    }
}
