package ui11.window;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Window extends SubstitutedWidget {

    private final String title;
    private final Widget content;

    @Inject private Slot contentSlot;

    public Window(String title, @NonNull Widget content) {
        // TODO title nullable?
        this.title = title;
        this.content = Objects.requireNonNull(content);
    }

    public String title() {
        return title;
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }

    // TODO return type pl. WidgetInstantiation?
    public static void open(Widget content) {
        Desktop.systemDesktop().openWindow(content);
    }
}
