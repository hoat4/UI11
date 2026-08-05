package ui11.input.pointer;

import org.jspecify.annotations.Nullable;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;
import java.util.function.Consumer;

public final class IsPressedListener extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @Nullable Consumer<Boolean> consumer;

    @Remember private Slot2 contentSlot;

    public IsPressedListener(@NonNull Widget content, @Nullable Consumer<Boolean> consumer) {
        this.content = Objects.requireNonNull(content);
        this.consumer = listenerProxy(consumer);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected IsPressedListener forSubstitution() {
        return new IsPressedListener(contentSlot.with(content), consumer);
    }

    public @NonNull Widget content() {
        return content;
    }

    public @Nullable Consumer<Boolean> consumer() {
        return consumer;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new IsPressedListenerImpl(this);
    }
}
