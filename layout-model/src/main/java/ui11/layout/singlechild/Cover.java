package ui11.layout.singlechild;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

/**
 * Resizes the child widget (while keeping its aspect ratio) to the smallest possible size to fill the container (that
 * is: both its height and width completely cover the container), leaving no empty space.
 * <p>
 * Similar to the {@code background-size: cover;} property value in CSS.
 */

// ez most középre igazít.
// TODO össze lehetne vonni Align-nal, és akkor meg nem csak középre tudna igazítani

// TODO DOM esetén csak URLImageView van támogatva ezen belül

public final class Cover extends SubstitutedWidget {

    private final Widget content;

    public Cover(Widget content) {
        this.content = content;
    }

    public Widget content() {
        return content;
    }
}
