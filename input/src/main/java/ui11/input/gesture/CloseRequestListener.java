package ui11.input.gesture;


import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

/**
 * Escape vagy böngésző back gomb vagy swipe. Nincs minden platformon támogatva, csak böngészők kb. 70%-án.
 */
public final class CloseRequestListener extends SubstitutedWidget {

    private final @NonNull Runnable onClose;
    private final @NonNull Widget content;

    public CloseRequestListener(@NonNull Runnable onClose, @NonNull Widget content) {
        this.onClose = listenerProxy(Objects.requireNonNull(onClose));
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Runnable onClose() {
        return onClose;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }
}
