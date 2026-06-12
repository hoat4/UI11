package ui11.input.pointer;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

// definiálni kéne hogy ez mit csinál pontosan, pl. lehet-e descendantot nem transparentté tenni

// TODO J2DPeer implementáció

public final class PointerTransparent extends SubstitutedWidget {

    private final Widget content;

    public PointerTransparent(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }
}
