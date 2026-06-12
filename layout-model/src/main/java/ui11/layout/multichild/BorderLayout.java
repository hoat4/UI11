package ui11.layout.multichild;

import ui11.Slot;
import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static ui11.graphics.Empty.empty;

public final class BorderLayout extends SubstitutedWidget {

    @Nullable private final Widget center;
    @Nullable private final Widget top;
    @Nullable private final Widget right;
    @Nullable private final Widget bottom;
    @Nullable private final Widget left;

    public BorderLayout(@Nullable Widget center,
                        @Nullable Widget top,
                        @Nullable Widget right,
                        @Nullable Widget bottom,
                        @Nullable Widget left) {
        this.center = center;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    public BorderLayout() {
        this(empty(), empty(), empty(), empty(), empty());
    }

    public BorderLayout center(Widget center) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout top(Widget top) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout right(Widget right) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout bottom(Widget bottom) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout left(Widget left) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout with(Side side, Widget content) {
        return switch (side) {
            case TOP -> top(content);
            case RIGHT -> right(content);
            case BOTTOM -> bottom(content);
            case LEFT -> left(content);
        };
    }

    @Nullable
    public Widget center() {
        return center;
    }

    @Nullable
    public Widget top() {
        return top;
    }

    @Nullable
    public Widget right() {
        return right;
    }

    @Nullable
    public Widget bottom() {
        return bottom;
    }

    @Nullable
    public Widget left() {
        return left;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new DefaultBorderLayoutImpl(this);
    }

    @Override
    public String toString() {
        return "BorderLayout[" +
                "center=" + center + ", " +
                "top=" + top + ", " +
                "right=" + right + ", " +
                "bottom=" + bottom + ", " +
                "left=" + left + ']';
    }


    public enum Side {
        TOP, RIGHT, BOTTOM, LEFT
    }
}

class DefaultBorderLayoutImpl extends Widget {

    private final BorderLayout l;

    @Inject private Slot topSlot;
    @Inject private Slot rightSlot;
    @Inject private Slot bottomSlot;
    @Inject private Slot leftSlot;
    @Inject private Slot centerSlot;

    public DefaultBorderLayoutImpl(BorderLayout l) {
        this.l = l;
    }

    @Override
    protected Widget build() {
        // TODO ha megcserélem erre, akkor Grid.add belezavarodik:
        //      add(left == null ? empty() : left, 1, 3).
        //      add(top == null ? empty() : top).
        //      add(right == null ? empty() : right, 1, 3).
        //      add(center == null ? empty() : center).
        //      add(bottom == null ? empty() : bottom).
        return Grid.builder(3).
                add(l.top() == null ? empty() : l.top().withSlot(topSlot), 3, 1).
                add(l.left() == null ? empty() : l.left().withSlot(leftSlot)).
                add(l.center() == null ? empty() : l.center().withSlot(centerSlot)).
                add(l.right() == null ? empty() : l.right().withSlot(rightSlot)).
                add(l.bottom() == null ? empty() : l.bottom().withSlot(bottomSlot), 3, 1).
                rowWeights(0, 1, 0).
                columnWeights(0, 1, 0).
                build();
    }
}
