package ui11.input.pointer;

import org.jspecify.annotations.Nullable;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;
import java.util.function.Consumer;

public final class IsPressedListener extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @Nullable Consumer<Boolean> consumer;

    @Inject private Slot contentSlot;

    public IsPressedListener(@NonNull Widget content, @Nullable Consumer<Boolean> consumer) {
        this.content = Objects.requireNonNull(content);
        this.consumer = listenerProxy(consumer);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public @Nullable Consumer<Boolean> consumer() {
        return consumer;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new IsPressedListenerImpl(this);
    }
}
