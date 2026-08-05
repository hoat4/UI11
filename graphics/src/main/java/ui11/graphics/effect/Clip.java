package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class Clip extends SubstitutedWidget {

    private final Widget content;

    @Remember private Slot2 contentSlot;

    public Clip(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Clip forSubstitution() {
        return new Clip(contentSlot.with(content));
    }

    public @NonNull Widget content() {
        return content;
    }
}
