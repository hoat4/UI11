package ui11.control.menu;

import ui11.*;
import ui11.geom.Vec2;
import ui11.observable.MutableObservable;
import ui11.animation.Scheduler;
import ui11.geom.Location;
import ui11.geom.Size;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.graphics.Surface;
import ui11.input.gesture.ClickListener;
import ui11.layout.singlechild.Align;
import ui11.layout.Gap;

import static ui11.graphics.effect.Overlay.overlay;
import static ui11.geom.Length.px;

public class MenuOverlay extends Widget {

    private final Location location;
    private final Runnable close;
    private final Widget menu;

    @Inject private Surface surface;
    @Inject private Scheduler scheduler;

    @Remember private MutableObservable<Boolean> hasSurface;
    @Remember private Vec2 p;
    @Remember private Size surfaceSizeAtOpen;

    public MenuOverlay(Location location, Runnable onClose, Widget menu) {
        this.location = location;
        this.close = listenerProxy(onClose);
        this.menu = menu;
    }

    @Override
    protected Widget build() {
        if (!hasSurface.get()) {
            // TODO ezt nem ilyen rondán. miért nem jó ha csak surface.get() == null-t nézünk?
            scheduler.runLater(() -> {
                p = location.in(surface.coordinateSpace());
                surfaceSizeAtOpen = surface.size();
                hasSurface.set(true);
            });
            return new ColorFill(Color.TRANSPARENT); // TODO itt empty() vagy pointerOpaque() kéne?
        }

        Widget xAlignedMenu = p.x() < surfaceSizeAtOpen.width() / 2 ?
                Align.right(
                        overlay(
                                Gap.horizontal(px(surfaceSizeAtOpen.width() - p.x())),
                                Align.left(menu)
                        )
                ) :
                Align.left(
                        overlay(
                                Gap.horizontal(px(p.x())),
                                Align.right(menu)
                        )
                );
        Align aligned = p.y() < surfaceSizeAtOpen.height() / 2 ?
                Align.bottom(
                        overlay(
                                Gap.vertical(px(surfaceSizeAtOpen.height() - p.y())),
                                Align.top(xAlignedMenu)
                        )
                ) :
                Align.top(
                        overlay(
                                Gap.vertical(px(p.y())),
                                Align.bottom(xAlignedMenu)
                        )
                );
        return overlay(
                new ClickListener(
                        new ColorFill(Color.parse("#0002")),
                        close
                ),
                aligned
        );
    }
}
