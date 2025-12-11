package ui11.platform.awt.j2d;

import ui11.Slot;
import ui11.Widget;
import ui11.geom.Location;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.effect.ClipPath;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.provide.UpValueWrapper;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class J2DClipPeer extends Widget {

    private final ClipPath clipPath;

    @Inject private Observable<Surface> parentSurface;
    @Inject private Slot contentSlot;

    @State private J2DClipPeerImpl state;

    public J2DClipPeer(ClipPath clipPath) {
        this.clipPath = clipPath;
    }

    @Override
    protected void initState() {
        state = new J2DClipPeerImpl();
    }

    @Override
    protected Widget build() {
        state.awtShape = J2DUtil.pathToJ2D(clipPath.shape());

        ((J2DSurface) parentSurface.get()).requestRepaint(); // TODO ne mindig

        Rectangle2D bounds = state.awtShape.getBounds2D();
        state.size.set(new Size(bounds.getMaxX(), bounds.getMaxY()));
        state.parentSurface.set((J2DSurface) parentSurface.get());
        Widget w = clipPath.content();
        w = new Provider<>(Surface.class, state, w);
        state.child = contentSlot.instantiate(w).lookup(J2DPrimitive.class);

        return new UpValueWrapper(state);
    }

    private static class J2DClipPeerImpl implements J2DPrimitive, J2DSurface {

        private Shape awtShape;
        private J2DPrimitive child;

        final MutableObservable<J2DSurface> parentSurface = MutableObservable.ofNullable();
        final MutableObservable<Size> size = MutableObservable.ofNullable();

        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
            Shape prevClip = g.getClip();
            g.clip(awtShape);

            child.draw(g, bounds);

            g.setClip(prevClip);
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            if (awtShape.contains(p.x(), p.y()))
                return child.findInputRegion(p);
            else
                return null;
        }


        @Override
        public Size size() {
            return size.get();
        }

        @Override
        public double devicePixelRatio() {
            return parentSurface.get().devicePixelRatio();
        }

        @Override
        public CoordinateSpace coordinateSpace() {
            return parentSurface.get().coordinateSpace();
        }

        @Override
        public boolean hitTest(Location point) {
            return parentSurface.get().hitTest(point);
        }

        @Override
        public void requestRepaint() {
            parentSurface.get().requestRepaint();
        }
    }
}
