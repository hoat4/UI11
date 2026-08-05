package ui11.input.pointer;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// definiálni kéne hogy ez mit csinál pontosan, pl. lehet-e descendantot nem transparentté tenni

// TODO J2DPeer implementáció

public final class PointerTransparent extends SubstitutedWidget {

    private final Widget content;

    @Remember private Slot2 contentSlot;

    public PointerTransparent(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected PointerTransparent forSubstitution() {
        return new PointerTransparent(contentSlot.with(content));
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
