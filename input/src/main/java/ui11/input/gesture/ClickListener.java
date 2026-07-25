package ui11.input.gesture;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.input.pointer.PointerOpaque;

import java.util.Objects;

// TODO ki kéne találni, hogy milyen területet akarunk érzékelni rákattintásnak.
//      DOM esetén azt tekintjük annak, ahol van valami nemüres elem,
//      AWT esetén viszont a ClickListener widget teljes területét.

// azért nem pointer package-ben van, mert enter lenyomás is kiválthatná
public final class ClickListener extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Runnable handler;

    @Inject private Slot contentSlot;

    public ClickListener(@Nullable Runnable handler) {
        this(PointerOpaque.pointerOpaque(), handler);
    }

    public ClickListener(@NonNull Widget content, @Nullable Runnable handler) {
        Objects.requireNonNull(content);
        if (handler == null)
            handler = () -> {
            };
        this.content = content;
        this.handler = listenerProxy(handler);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public @NonNull Runnable handler() {
        return handler;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new ClickListenerImpl(this);
    }
}
