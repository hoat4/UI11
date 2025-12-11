package ui11.input.pointer;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

public final class IsPressedListener extends SubstitutedWidget {

    private final Widget content;
    @Listener private final Consumer<Boolean> consumer;

    public IsPressedListener(Widget content, Consumer<Boolean> consumer) {
        Objects.requireNonNull(content);
        this.content = content;
        this.consumer = consumer;
    }

    public Widget content() {
        return content;
    }

    public Consumer<Boolean> consumer() {
        return consumer;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new IsPressedListenerImpl(this);
    }
}
