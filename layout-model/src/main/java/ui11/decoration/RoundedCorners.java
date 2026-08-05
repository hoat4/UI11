package ui11.decoration;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

// TODO ez duplikálva van, van ez, meg graphics.RoundedCorners is

// TODO négy külön radius kéne

// TODO az ebbe rakott border nem fog lekerekítődni

public final class RoundedCorners extends SubstitutedWidget {

    private final Length radius;
    private final Widget content;

    @Remember private Slot2 contentSlot;

    public RoundedCorners(@NonNull Length radius, @NonNull Widget content) {
        this.radius = Objects.requireNonNull(radius);
        this.content = Objects.requireNonNull(content);
    }

    public static RoundedCorners withCorners(@NonNull Length radius, @NonNull Widget content) {
        return new RoundedCorners(radius, content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected RoundedCorners forSubstitution() {
        return new RoundedCorners(
                radius,
                contentSlot.with(content)
        );
    }

    public @NonNull Length radius() {
        return radius;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        /*
        ez most úgyse működik, mert content be van wrappelve SlotWidgetbe
        if (content instanceof Box b && b.cornerRadius().isZero())
            return b.withCornerRadius(radius).withSlot(contentSlot);
        else
         */
        return new Box(content()).withCornerRadius(radius);
    }
}
