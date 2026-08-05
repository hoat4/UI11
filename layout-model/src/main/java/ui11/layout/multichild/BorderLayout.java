package ui11.layout.multichild;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static ui11.graphics.Empty.empty;

public final class BorderLayout extends SubstitutedWidget {

    private final @Nullable Widget center;
    private final @Nullable Widget top;
    private final @Nullable Widget right;
    private final @Nullable Widget bottom;
    private final @Nullable Widget left;

    @Remember private Slot2 centerSlot;
    @Remember private Slot2 topSlot;
    @Remember private Slot2 rightSlot;
    @Remember private Slot2 bottomSlot;
    @Remember private Slot2 leftSlot;

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
        this(null, null, null, null, null);
    }

    @Override
    protected void initState() {
        centerSlot = new Slot2();
        topSlot = new Slot2();
        rightSlot = new Slot2();
        bottomSlot = new Slot2();
        leftSlot = new Slot2();
    }

    @Override
    protected BorderLayout cloneForSubstitution() {
        return new BorderLayout(
                centerSlot.with(center),
                topSlot.with(top),
                rightSlot.with(right),
                bottomSlot.with(bottom),
                leftSlot.with(left)
        );
    }

    public BorderLayout center(@Nullable Widget center) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout top(@Nullable Widget top) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout right(@Nullable Widget right) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout bottom(@Nullable Widget bottom) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout left(@Nullable Widget left) {
        return new BorderLayout(center, top, right, bottom, left);
    }

    public BorderLayout with(@NonNull Side side, @Nullable Widget content) {
        return switch (side) {
            case TOP -> top(content);
            case RIGHT -> right(content);
            case BOTTOM -> bottom(content);
            case LEFT -> left(content);
        };
    }

    public @Nullable Widget center() {
        return center;
    }

    public @Nullable Widget top() {
        return top;
    }

    public @Nullable Widget right() {
        return right;
    }

    public @Nullable Widget bottom() {
        return bottom;
    }

    public @Nullable Widget left() {
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
                add(l.top() == null ? empty() : l.top(), 3, 1).
                add(l.left() == null ? empty() : l.left()).
                add(l.center() == null ? empty() : l.center()).
                add(l.right() == null ? empty() : l.right()).
                add(l.bottom() == null ? empty() : l.bottom(), 3, 1).
                rowWeights(0, 1, 0).
                columnWeights(0, 1, 0).
                build();
    }
}
