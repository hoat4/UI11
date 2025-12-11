package ui11.window;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class Window extends SubstitutedWidget {
    private final String title;
    private final Widget content;

    public Window(String title, Widget content) {
        Objects.requireNonNull(content);
        // TODO title nullable?
        this.title = title;
        this.content = content;
    }

    public String title() {
        return title;
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
