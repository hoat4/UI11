package ui11.control;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class Tooltip extends SubstitutedWidget {

    private final String tooltip;
    private final Widget content;

    @Remember private Slot2 contentSlot;

    public Tooltip(@NonNull String tooltip, @NonNull Widget content) {
        this.tooltip = Objects.requireNonNull(tooltip);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Tooltip forSubstitution() {
        return new Tooltip(
                tooltip,
                contentSlot.with(content)
        );
    }

    public @NonNull String tooltip() {
        return tooltip;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected Widget fallbackContent() {
        return content();
    }
}
