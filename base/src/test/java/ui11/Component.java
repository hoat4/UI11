package ui11;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

abstract class Component extends Widget {

    // TODO inkább valami @Transient kéne ehelyett
    @Remember
    private List<Widget> childComponents;

    protected abstract void update();

    @Override
    protected final Widget build() {
        childComponents = new ArrayList<>();
        update();
        List<Widget> childComponents2 = childComponents;
        childComponents = null;
        return PeerRequestor.ofMultipleWidgets(childComponents2, ComponentResultRequest.INSTANCE,
                resolutionResults -> ComponentResult.INSTANCE);
    }

    protected void useComponent(Widget component) {
        Objects.requireNonNull(component);
        if (childComponents == null)
            throw new IllegalStateException();
        childComponents.add(component);
    }

    protected void useComponent(Widget component, PeerRequestor.Request<?> request) {
        Objects.requireNonNull(component);
        if (childComponents == null)
            throw new IllegalStateException();
        childComponents.add(PeerRequestor.ofSingle(component, request,
                result -> ComponentResult.INSTANCE));
    }

    static class ComponentResult extends SubstitutedWidget {
        static final ComponentResult INSTANCE = new ComponentResult();
    }

    private static class ComponentResultRequest extends PeerRequestor.Request<ComponentResult> {

        private static final ComponentResultRequest INSTANCE = new ComponentResultRequest();

        private ComponentResultRequest() {
            super(ComponentResult.class);
        }

        public static <R> ComponentResultRequest instance() {
            return INSTANCE;
        }
    }
}
