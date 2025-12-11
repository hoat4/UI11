package ui11.platform.awt.j2d;

import ui11.Slot;
import ui11.Widget;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Mat4;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.effect.Transform;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.provide.Provider;
import ui11.provide.UpValueWrapper;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D.Double;
import java.util.Objects;

public class J2DTransformPeer extends Widget {

    private final Transform transform;

    @Inject private Observable<Surface> parentSurface;
    @Inject private Slot transformedContentSlot;

    @State private J2DTransformPeerImpl state;

    public J2DTransformPeer(Transform transform) {
        this.transform = transform;
    }

    @Override
    protected void initState() {
        state = new J2DTransformPeerImpl();
    }

    @Override
    protected Widget build() {
        state.parentSurface.set(parentSurface.get());
        state.update(transform.transformation());

        // ezt a size beállítás után kell, hogy child tudja hivatkozni Surface.size-on keresztül
        J2DPrimitive newChild = transformedContentSlot.instantiate(
                        new Provider<>(Surface.class, state, transform.content())).
                lookup(J2DPrimitive.class);
        if (newChild != state.child) {
            if (state.child != null)
                ((J2DSurface) parentSurface.get()).requestRepaint();
            state.child = newChild;
        }

        return new UpValueWrapper(state);
    }

    private static class J2DTransformPeerImpl implements J2DPrimitive, J2DSurface {

        J2DPrimitive child;
        private final AffineTransform awtAffineTransformation = new AffineTransform();
        private final MutableObservable<Size> size = MutableObservable.ofNullable();
        private final MutableObservable<Surface> parentSurface = MutableObservable.ofNullable();

        void update(Mat4 t) {
            awtAffineTransformation.setTransform(
                    t.m00(), t.m01(),
                    t.m10(), t.m11(),
                    t.m30(), t.m31()
            );

            Surface parentSurface1 = this.parentSurface.get();
            size.set(Objects.requireNonNull(parentSurface1.size()));

            ((J2DSurface) parentSurface1).requestRepaint();
        }

        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
        /*
        g.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
        g.fill(bounds);
        g.setColor(Color.WHITE);
        g.drawString("T"+size.snoop().height(), 0, g.getFontMetrics().getAscent());
         */

            AffineTransform prev = g.getTransform();
            g.transform(awtAffineTransformation);

            Size size = this.size.get();
            Rectangle childBounds = size == null ? bounds :
                    new Rectangle(0, 0, (int) Math.ceil(size.width()), (int) Math.ceil(size.height()));

            child.draw(g, childBounds);

            g.setTransform(prev);
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            Double p2 = new Double(p.x(), p.y());
            try {
                awtAffineTransformation.inverseTransform(p2, p2);
            } catch (NoninvertibleTransformException e) {
                // nem látszik a képernyőn a child, tehát nem lehet rákattintani sem
                return null;
            }
            return child.findInputRegion(new Vec2(p2.x, p2.y));
        }

        @Override
        public Size size() {
            Size s = size.get();
            if (s == null)
                throw new IllegalStateException();
            return s;
        }

        @Override
        public void requestRepaint() {
            ((J2DSurface) parentSurface.get()).requestRepaint();
        }

        @Override
        public double devicePixelRatio() {
            return parentSurface.get().devicePixelRatio();
        }

        @Override
        public CoordinateSpace coordinateSpace() {
            throw new RuntimeException("TODO");
        }
    }
}
