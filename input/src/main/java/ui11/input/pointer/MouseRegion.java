package ui11.input.pointer;

import ui11.*;
import ui11.geom.Location;
import ui11.input.pointer.Pointer.Button;
import ui11.input.pointer.PointerRegion.PointerListener;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;

import java.util.Objects;

public final class MouseRegion extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Button acceptedButton;
    private final @NonNull MouseListener listener;

    @Remember private Slot2 contentSlot;

    public MouseRegion(@NonNull Widget content, @NonNull Button acceptedButton, @NonNull MouseListener listener) {
        this.content = Objects.requireNonNull(content);
        this.acceptedButton = Objects.requireNonNull(acceptedButton);
        this.listener = Objects.requireNonNull(listener); // TODO listenerProxy
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected MouseRegion forSubstitution() {
        return new MouseRegion(
                contentSlot.with(content),
                acceptedButton,
                listener
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Button acceptedButton() {
        return acceptedButton;
    }

    public @NonNull MouseListener listener() {
        return listener;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
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

    @Remember private MouseRegionImplState state;

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
