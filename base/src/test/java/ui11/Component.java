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
        return ComponentResultRequest.INSTANCE.executedOn(childComponents2,
                resolutionResults -> ComponentResult.INSTANCE);
    }

    protected void useComponent(Slot slot, Widget component) {
        Objects.requireNonNull(slot);
        Objects.requireNonNull(component);
        if (childComponents == null)
            throw new IllegalStateException();
        childComponents.add(component.withSlot(slot));
    }

    protected void useComponent(Slot slot, Widget component, PeerCreationRequest<?> request) {
        Objects.requireNonNull(slot);
        Objects.requireNonNull(component);
        if (childComponents == null)
            throw new IllegalStateException();
        childComponents.add(request.executedOn(component.withSlot(slot),
                result -> ComponentResult.INSTANCE));
    }

    static class ComponentResult extends SubstitutedWidget {
        static final ComponentResult INSTANCE = new ComponentResult();
    }

    private static class ComponentResultRequest extends PeerCreationRequest<ComponentResult> {

        private static final ComponentResultRequest INSTANCE = new ComponentResultRequest();

        private ComponentResultRequest() {
            super(ComponentResult.class);
        }

        public static <R> ComponentResultRequest instance() {
            return INSTANCE;
        }
    }
}
