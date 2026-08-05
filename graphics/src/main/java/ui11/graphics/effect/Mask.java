package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// TODO mask módok (luminosity, alpha, stb.)

public final class Mask extends SubstitutedWidget {

    private final Widget content;
    private final Widget mask;

    @Remember private Slot contentSlot;
    @Remember private Slot maskSlot;

    public Mask(@NonNull Widget content, @NonNull Widget mask) {
        this.content = Objects.requireNonNull(content);
        this.mask = Objects.requireNonNull(mask);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot();
        maskSlot = new Slot();
    }

    @Override
    protected Mask forSubstitution() {
        return new Mask(
                contentSlot.with(content),
                maskSlot.with(mask)
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Widget mask() {
        return mask;
    }
}
