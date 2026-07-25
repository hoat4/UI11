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

    @Inject private Slot maskSlot;
    @Inject private Slot contentSlot;

    public Mask(@NonNull Widget content, @NonNull Widget mask) {
        this.content = Objects.requireNonNull(content);
        this.mask = Objects.requireNonNull(mask);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public @NonNull Widget mask() {
        return maskSlot == null ? mask : mask.withSlot(maskSlot);
    }
}
