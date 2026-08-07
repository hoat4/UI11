package ui11.window;

import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Window extends SubstitutedWidget {

    private final String title;
    private final Widget content;

    @Remember private Key contentKey;

    public Window(String title, @NonNull Widget content) {
        // TODO title nullable?
        this.title = title;
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected Window forSubstitution() {
        return new Window(
                title,
                content.withKey(contentKey)
        );
    }

    public String title() {
        return title;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }

    // TODO return type pl. WidgetInstantiation?
    // az nem jó, mert már nem publikus
    public static void open(Widget content) {
        Desktop.systemDesktop().openWindow(content);
    }
}
