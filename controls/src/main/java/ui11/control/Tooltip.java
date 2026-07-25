package ui11.control;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class Tooltip extends SubstitutedWidget {

    private final String tooltip;
    private final Widget content;

    @Inject private Slot contentSlot;

    public Tooltip(@NonNull String tooltip, @NonNull Widget content) {
        this.tooltip = Objects.requireNonNull(tooltip);
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull String tooltip() {
        return tooltip;
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    @Override
    protected Widget fallbackContent() {
        return content();
    }
}
