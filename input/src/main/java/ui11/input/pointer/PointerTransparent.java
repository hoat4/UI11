package ui11.input.pointer;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// definiálni kéne hogy ez mit csinál pontosan, pl. lehet-e descendantot nem transparentté tenni

// TODO J2DPeer implementáció

public final class PointerTransparent extends SubstitutedWidget {

    private final Widget content;

    @Inject private Slot contentSlot;

    public PointerTransparent(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
