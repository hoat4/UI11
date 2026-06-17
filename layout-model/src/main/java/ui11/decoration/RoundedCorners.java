package ui11.decoration;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;

// TODO ez duplikálva van, van ez, meg graphics.RoundedCorners is

// TODO négy külön radius kéne

// TODO az ebbe rakott border nem fog lekerekítődni

public final class RoundedCorners extends SubstitutedWidget {

    private final Length radius;
    private final Widget content;

    public RoundedCorners(Length radius, Widget content) {
        this.radius = radius;
        this.content = content;
    }

    public static RoundedCorners withCorners(Length radius, Widget content) {
        return new RoundedCorners(radius, content);
    }

    public Length radius() {
        return radius;
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        if (content instanceof Box b && b.cornerRadius().isZero())
            return b.withCornerRadius(radius);
        else
            return new Box(content).withCornerRadius(radius);
    }
}
