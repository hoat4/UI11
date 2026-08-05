package ui11.layout.singlechild;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * Layout és input szempontból is rejtett lesz, nem csak grafikailag
 * (tehát kb. mint Androidban GONE, CSS-ben display:none).
 */
// TODO leírni hogy mi a különbség Gone-hoz képest
public final class Hidden extends SubstitutedWidget {

    private final @NonNull Widget content;

    @Remember private Slot2 contentSlot;

    // egyelőre csak abban különbözik attól ha csak child lenne de widget fában nem szerepl, hogy
    // scroll pozíció megőrződik.

    public Hidden(@NonNull Widget content)  {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Hidden forSubstitution() {
        return new Hidden(contentSlot.with(content));
    }

    public @NonNull Widget content() {
        return content;
    }
}
