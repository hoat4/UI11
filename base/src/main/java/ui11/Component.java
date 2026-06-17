package ui11;

// TODO javadoc frissítése

/**
 * Egy szülő Element életciklusához kapcsolódik, de saját maga nem tartalmaz widgetet.
 */
public abstract class Component<R> extends Widget {

    protected abstract R update();

    @Override
    protected final Widget build() {
        return new ComponentResultUpValue<>(update());
    }

    static class ComponentResultUpValue<R> extends SubstitutedWidget {

        final R result;

        ComponentResultUpValue(R result) {
            this.result = result;
        }
    }

    static class ComponentResultRequest<R> extends PeerCreationRequest<ComponentResultUpValue<R>> {

        private static final ComponentResultRequest<?> INSTANCE = new ComponentResultRequest<>();

        @SuppressWarnings("unchecked")
        private ComponentResultRequest() {
            super((Class<ComponentResultUpValue<R>>) (Class<?>) ComponentResultUpValue.class);
        }

        @SuppressWarnings("unchecked")
        public static <R> ComponentResultRequest<R> instance() {
            return (ComponentResultRequest<R>) INSTANCE;
        }
    }
}
