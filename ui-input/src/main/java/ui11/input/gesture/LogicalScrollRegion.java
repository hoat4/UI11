package ui11.input.gesture;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

// vízszintes is kéne?
// TODO ez nincs használva
public final class LogicalScrollRegion extends SubstitutedWidget {

    @Listener private final Consumer<LogicalScroll> consumer; // ez lehetne nemnull
    @Nonnull private final Widget content;

    public LogicalScrollRegion(Consumer<LogicalScroll> consumer, @Nonnull Widget content) {
        this.consumer = consumer;
        this.content = content;
    }

    public Consumer<LogicalScroll> consumer() {
        return consumer;
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return content;
    }

    public record LogicalScroll(Unit unit, int delta) {

        public enum Unit {
            LINE, PAGE
        }
    }
}