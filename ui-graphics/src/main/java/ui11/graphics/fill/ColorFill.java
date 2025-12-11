package ui11.graphics.fill;

import ui11.SubstitutedWidget;

public final class ColorFill extends SubstitutedWidget {

    private final Color color;

    public ColorFill(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }

    @Override
    public String toString() {
        return "ColorFill " + color;
    }
}
