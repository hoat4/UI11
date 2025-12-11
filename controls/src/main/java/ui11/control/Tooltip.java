package ui11.control;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class Tooltip extends SubstitutedWidget {

    private final String tooltip;
    private final Widget content;

    public Tooltip(String tooltip, Widget content) {
        this.tooltip = Objects.requireNonNull(tooltip);
        this.content = Objects.requireNonNull(content);
    }

    public String tooltip() {
        return tooltip;
    }

    public Widget content() {
        return content;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return content;
    }
}
