package ui11.layout;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Axis;
import ui11.layout.singlechild.Padding;
import ui11.geom.Length;

import javax.annotation.Nonnull;
import java.util.Objects;

import static ui11.graphics.Empty.empty;
import static ui11.geom.Length.zero;

public final class Gap extends SubstitutedWidget {

    @Nonnull private final Axis axis;
    @Nonnull private final Length length;

    public Gap(@Nonnull Axis axis, @Nonnull Length length) {
        this.axis = Objects.requireNonNull(axis);
        this.length = Objects.requireNonNull(length);
    }

    public static Gap horizontal(@Nonnull Length length) {
        return new Gap(Axis.HORIZONTAL, length);
    }

    public static Gap vertical(@Nonnull Length length) {
        return new Gap(Axis.VERTICAL, length);
    }

    @Nonnull
    public Axis axis() {
        return axis;
    }

    @Nonnull
    public Length length() {
        return length;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new Padding(switch (axis) {
            case HORIZONTAL -> new Insets(zero(), zero(), zero(), length);
            case VERTICAL -> new Insets(length, zero(), zero(), zero());
        }, empty());
    }
}
