package ui11;

import java.util.Objects;

/**
 * <a href="https://api.flutter.dev/flutter/widgets/ParentDataWidget-class.html">Same concept in Flutter</a>
 */
public abstract class ParentDataWidget extends SubstitutedWidget {

    private final Widget next;

    protected ParentDataWidget(Widget next) {
        this.next = Objects.requireNonNull(next);
    }

    public static ParentDataWidget of(SubstitutedWidget parentData, Widget content) {
        return new CombinerParentDataWidget(parentData, content);
    }

    @Override
    protected final Widget fallbackContent() {
        return next;
    }

    static class CombinerParentDataWidget extends ParentDataWidget {

        final SubstitutedWidget parentData;

        protected CombinerParentDataWidget(SubstitutedWidget parentData, Widget next) {
            super(next);
            this.parentData = parentData;
        }
    }
}
