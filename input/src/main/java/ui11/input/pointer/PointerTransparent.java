package ui11.input.pointer;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

// definiálni kéne hogy ez mit csinál pontosan, pl. lehet-e descendantot nem transparentté tenni

// TODO J2DPeer implementáció

public final class PointerTransparent extends SubstitutedWidget {

    private final Widget content;

    public PointerTransparent(@Nonnull Widget content) {
        this.content = Objects.requireNonNull(content);
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
