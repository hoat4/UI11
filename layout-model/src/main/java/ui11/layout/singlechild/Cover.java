package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

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

    @Remember private Slot2 contentSlot;

    public Cover(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Cover forSubstitution() {
        return new Cover(contentSlot.with(content));
    }

    public @NonNull Widget content() {
        return content;
    }
}
