package ui11.layout;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Axis;
import ui11.layout.singlechild.Padding;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

import static ui11.graphics.Empty.empty;
import static ui11.geom.Length.zero;

public final class Gap extends SubstitutedWidget {

    private final @NonNull Axis axis;
    private final @NonNull Length length;

    public Gap(@NonNull Axis axis, @NonNull Length length) {
        this.axis = Objects.requireNonNull(axis);
        this.length = Objects.requireNonNull(length);
    }

    public static Gap horizontal(@NonNull Length length) {
        return new Gap(Axis.HORIZONTAL, length);
    }

    public static Gap vertical(@NonNull Length length) {
        return new Gap(Axis.VERTICAL, length);
    }

    public @NonNull Axis axis() {
        return axis;
    }

    public @NonNull Length length() {
        return length;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new Padding(switch (axis) {
            case HORIZONTAL -> new Insets(zero(), zero(), zero(), length);
            case VERTICAL -> new Insets(length, zero(), zero(), zero());
        }, empty());
    }
}
