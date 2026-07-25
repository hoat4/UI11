package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class Clip extends SubstitutedWidget {

    private final Widget content;

    @Inject private Slot contentSlot;

    public Clip(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }
}
