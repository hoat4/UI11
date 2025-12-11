package ui11.input.gesture;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.resolution.DefaultPeer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

// TODO ki kéne találni, hogy milyen területet akarunk érzékelni rákattintásnak.
//      DOM esetén azt tekintjük annak, ahol van valami nemüres elem,
//      AWT esetén viszont a ClickListener widget teljes területét.

// azért nem pointer package-ben van, mert enter lenyomás is kiválthatná
public final class ClickListener extends SubstitutedWidget {

    @Nonnull private final Widget content;
    @Nonnull @Listener private final Runnable handler;

    public ClickListener(@Nonnull Widget content, @Nullable Runnable handler) {
        Objects.requireNonNull(content);
        if (handler == null)
            handler = () -> {
            };
        this.content = content;
        this.handler = handler;
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    public Runnable handler() {
        return handler;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new ClickListenerImpl(this);
    }
}
