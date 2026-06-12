package ui11.control.menu;

import ui11.*;
import ui11.geom.Vec2;
import ui11.observable.SimpleScope;
import ui11.control.DialogContainer.DialogContainerState;
import ui11.geom.Location;
import ui11.geom.Rect;
import ui11.graphics.Surface;
import ui11.input.pointer.Pointer.StandardMouseButton;
import ui11.input.gesture.ClickListener;
import ui11.input.pointer.MouseRegion;
import ui11.input.pointer.MouseRegion.MouseListener;

import org.jspecify.annotations.Nullable;
import java.util.function.Function;

public class PopupMenuOpener extends Widget {

    private final Widget content;
    private final Function<SimpleScope, ? extends Widget> menuSupplier;
    @Nullable private final Function<Rect, Vec2> openAt;

    @Inject private Surface surface;
    @Inject private DialogContainerState dialogContainerState;

    // TODO openAt equals?
    //      lehet hogy kéne @Inputba egy boolean, hogy ha megváltozik, akkor maradjuk a régi RSW példánynál,
    //      de ezt a megzőt másoljuk át a régibe

    // TODO nem kéne a bottomCenternek defaultnak lennie
    public PopupMenuOpener(Widget content, Function<SimpleScope, ? extends Widget> menuSupplier) {
        this(content, menuSupplier, Rect::bottomCenter);
    }

    /**
     * @param content
     * @param menuSupplier
     * @param openAt       ha ez null, akkor az egérkattintás helyénél fog megjelenni a popup
     */
    public PopupMenuOpener(Widget content, Function<SimpleScope, ? extends Widget> menuSupplier,
                           @Nullable Function<Rect, Vec2> openAt) {
        this.content = content;
        this.menuSupplier = menuSupplier;
        this.openAt = openAt;
    }

    @Override
    protected Widget build() {
        if (openAt == null)
            return new MouseRegion(content, StandardMouseButton.PRIMARY, new MouseListener() {

                private Location location;

                @Override
                public void hoverMoved(Location location) {
                }

                @Override
                public void hoverMovedOut() {
                }

                @Override
                public void down(Location location) {
                    this.location = location;
                }

                @Override
                public void drag(Location location) {
                    this.location = location;
                }

                @Override
                public void up() {
                    openMenuAt(location);
                }

                @Override
                public void cancel() {
                }
            });
        else {
            return new ClickListener(
                    content,
                    () -> {
                        Rect rect = Rect.of(surface.size());
                        Location pos = new Location(surface.coordinateSpace(),
                                openAt.apply(rect));
                        openMenuAt(pos);
                    }
            );
        }
    }

    private void openMenuAt(Location pos) {
        SimpleScope scope = new SimpleScope(untilPause());
        dialogContainerState.open(new MenuOverlay(pos, scope::close,
                menuSupplier.apply(scope)), scope);
    }
}

