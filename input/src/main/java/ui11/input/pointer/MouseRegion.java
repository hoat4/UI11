package ui11.input.pointer;

import ui11.*;
import ui11.geom.Location;
import ui11.input.pointer.Pointer.Button;
import ui11.input.pointer.PointerRegion.PointerListener;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class MouseRegion extends SubstitutedWidget {

    @Nonnull private final Widget content;
    @Nonnull private final Button acceptedButton;
    @Nonnull private final MouseListener listener;

    public MouseRegion(@Nonnull Widget content, @Nonnull Button acceptedButton, @Nonnull MouseListener listener) {
        this.content = Objects.requireNonNull(content);
        this.acceptedButton = Objects.requireNonNull(acceptedButton);
        this.listener = Objects.requireNonNull(listener);
    }

    public Widget content() {
        return content;
    }

    public Button acceptedButton() {
        return acceptedButton;
    }

    public MouseListener listener() {
        return listener;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new MouseRegionImpl(this);
    }

    public interface MouseListener {

        // TODO a hoverMoved/hoverMovedOut meghívódik most pressed állapotban is, de csak akkor ha másik pointer
        //      az ami hoverol. ez jó így?

        /**
         * Ez csak akkor hívódik meg, ha a mutató az elemen belül van
         */
        void hoverMoved(Location location);

        void hoverMovedOut();

        void down(Location location);

        void drag(Location location);

        void up();

        void cancel();
    }
}

class MouseRegionImpl extends Widget {

    private static final boolean TRACE = false;

    private final MouseRegion mouseRegion;

    @State private MouseRegionImplState state;

    // TODO lehet hogy el kéne tárolni egy hover pointert is, hogy ne kavarodjon össze
    //      ha két pointer két helyen vannak és akkor össze-vissza ugrál.

    public MouseRegionImpl(MouseRegion mouseRegion) {
        this.mouseRegion = mouseRegion;
    }

    @Override
    protected void initState() {
        state = new MouseRegionImplState();
    }

    @Override
    protected Widget build() {
        state.mouseRegion = mouseRegion;
        // TODO kéne valamit csinálni, hogy lehessen PointerRegionokat a state-es kavarás nélkül is használni
        return new PointerRegion(mouseRegion.content()) {

            @Override
            public PointerListener onPointerDown(Pointer pointer, Button button) {
                Objects.requireNonNull(pointer);
                if (TRACE)
                    System.out.println("opd " + state.activePointer + ", " + button + ", " + mouseRegion.acceptedButton());
                if (state.activePointer == null && button.equals(mouseRegion.acceptedButton())) {
                    state.activePointer = pointer;
                    mouseRegion.listener().down(pointer.location());
                    return state.down();
                } else
                    return null;
            }

            @Override
            public void onPointerMove(Pointer pointer, boolean inside) {
                if (!pointer.equals(state.activePointer)) {
                    if (inside)
                        mouseRegion.listener().hoverMoved(pointer.location());
                    else
                        mouseRegion.listener().hoverMovedOut();
                }
            }
        };
    }

    private static class MouseRegionImplState {

        Pointer activePointer;
        MouseRegion mouseRegion;

        PointerListener down() {
            return new PointerListener() {

                private boolean released;

                @Override
                public void onPress(Button button) {
                }

                @Override
                public void onMove() {
                    if (!released)
                        mouseRegion.listener().drag(activePointer.location());
                }

                @Override
                public void onRelease(Button button) {
                    if (!released && button.equals(mouseRegion.acceptedButton())) {
                        if (TRACE)
                            System.out.println("onRelease " + this + ", " + activePointer);
                        released = true;
                        activePointer = null;
                        mouseRegion.listener().up();
                    }
                }

                @Override
                public void onFinish() {
                    if (!released) {
                        if (TRACE)
                            System.out.println("onFinish " + this + ", " + activePointer);
                        released = true;
                        activePointer = null;
                        mouseRegion.listener().cancel();
                    }
                }
            };
        }
    }
}
