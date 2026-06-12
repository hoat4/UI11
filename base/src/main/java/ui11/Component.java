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

    static class ComponentResultUpValue<R> extends EndingWidget {

        final R result;

        ComponentResultUpValue(R result) {
            this.result = result;
        }
    }
}
