package ui11.graphics.fill;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.color.Color;

import java.util.Objects;

public final class ColorFill extends SubstitutedWidget {

    private final @NonNull Color color;

    public ColorFill(@NonNull Color color) {
        Objects.requireNonNull(color, "color");
        this.color = color;
    }

    public @NonNull Color color() {
        return color;
    }

    @Override
    public String toString() {
        return "ColorFill " + color;
    }
}
