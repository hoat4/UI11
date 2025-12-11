package ui11.input.pointer;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.geom.Location;
import ui11.graphics.Surface;
import ui11.input.pointer.MouseRegion.MouseListener;
import ui11.input.pointer.Pointer.StandardMouseButton;

import javax.annotation.Nonnull;

// itt specifikálni kéne, hogy egérállapot-váltáskor ezek egymás helyébe lépnek
// (mert ugyanaz az implicit keyük).
// és hogy nem lesznek aktív Elementek azok, amelyek nem aktuálisak
// (ha valaki nagyon akarja ezt, akkor megadja a 3 widgetnek
// Element::instantiate resultjait).
public final class PointerStateDependent extends SubstitutedWidget {

    private final Widget defaultContent;
    private final Widget hoverContent;
    private final Widget pressedContent;

    public PointerStateDependent(
            Widget defaultContent,
            Widget hoverContent,
            Widget pressedContent) {
        this.defaultContent = defaultContent;
        this.hoverContent = hoverContent;
        this.pressedContent = pressedContent;
    }

    public Widget defaultContent() {
        return defaultContent;
    }

    public Widget hoverContent() {
        return hoverContent;
    }

    public Widget pressedContent() {
        return pressedContent;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new PointerStateDependentImpl(this);
    }
}

class PointerStateDependentImpl extends Widget {

    private final PointerStateDependent pointerStateDependent;

    @Inject private Observable<Surface> surface;

    @State private MutableObservable<Boolean> isHover;
    @State private MutableObservable<PressState> isPressed;

    public PointerStateDependentImpl(PointerStateDependent pointerStateDependent) {
        this.pointerStateDependent = pointerStateDependent;
    }

    @Override
    protected void initState() {
        isHover = MutableObservable.withInitial(false);
        isPressed = MutableObservable.withInitial(PressState.NOT_PRESSED);
    }

    @Override
    protected Widget build() {
        Widget content = switch (isPressed.get()) {
            case NOT_PRESSED -> isHover.get() ?
                    pointerStateDependent.hoverContent() :
                    pointerStateDependent.defaultContent();
            case PRESSED -> pointerStateDependent.pressedContent();
            case DRAGGED_OUT -> pointerStateDependent.defaultContent();
        };

        return new MouseRegion(content, StandardMouseButton.PRIMARY, new MouseListener() {
            @Override
            public void hoverMoved(Location location) {
                isHover.set(true);
            }

            @Override
            public void hoverMovedOut() {
                isHover.set(false);
            }

            @Override
            public void down(Location location) {
                isPressed.set(PressState.PRESSED);
            }

            @Override
            public void drag(Location location) {
                boolean hover = surface.get().hitTest(location);
                isHover.set(hover);
                isPressed.set(hover ? PressState.PRESSED : PressState.DRAGGED_OUT);
            }

            @Override
            public void up() {
                isPressed.set(PressState.NOT_PRESSED);
            }

            @Override
            public void cancel() {
                isPressed.set(PressState.NOT_PRESSED);
            }
        });
    }

    private enum PressState {
        NOT_PRESSED, PRESSED, DRAGGED_OUT
    }
}
