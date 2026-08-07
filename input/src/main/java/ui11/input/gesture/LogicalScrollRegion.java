package ui11.input.gesture;

import org.jspecify.annotations.Nullable;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.Consumer;

// vízszintes is kéne?
// TODO ez nincs használva
public final class LogicalScrollRegion extends SubstitutedWidget {

    private final @Nullable Consumer<LogicalScroll> consumer; // ez lehetne nemnull
    private final @NonNull Widget content;

    @Remember private Key contentKey;

    public LogicalScrollRegion(Consumer<LogicalScroll> consumer, @NonNull Widget content) {
        this.consumer = listenerProxy(consumer);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected LogicalScrollRegion forSubstitution() {
        return new LogicalScrollRegion(consumer, content.withKey(contentKey));
    }

    public @Nullable Consumer<LogicalScroll> consumer() {
        return consumer;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }

    public record LogicalScroll(Unit unit, int delta) {

        public enum Unit {
            LINE, PAGE
        }
    }
}