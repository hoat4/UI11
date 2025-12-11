package ui11.input.gesture;


import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Escape vagy böngésző back gomb vagy swipe. Nincs minden platformon támogatva, csak böngészők kb. 70%-án.
 */
public final class CloseRequestListener extends SubstitutedWidget {

    @Listener @Nonnull private final Runnable onClose;
    @Nonnull private final Widget content;

    public CloseRequestListener(@Nonnull Runnable onClose, @Nonnull Widget content) {
        this.onClose = Objects.requireNonNull(onClose);
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public Runnable onClose() {
        return onClose;
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
}
