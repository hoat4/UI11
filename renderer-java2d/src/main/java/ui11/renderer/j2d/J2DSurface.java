package ui11.renderer.j2d;

import ui11.geom.Location;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.renderer.j2d.J2DSurface.J2DSurfaceWithOwnShape;
import ui11.renderer.j2d.J2DSurface.ShapeInheritingJ2DSurface;
import ui11.renderer.j2d.rendertree.FillPathRenderNode;
import ui11.renderer.j2d.rendertree.RenderNode;

import java.awt.*;
import java.awt.geom.Rectangle2D;

// TODO @Inject Surface most nem működik

public abstract sealed class J2DSurface
        extends Surface<J2DNodeHolder>
        permits J2DSurfaceWithOwnShape, ShapeInheritingJ2DSurface {

    public static final Shape INFINITE_SHAPE = new Rectangle2D.Double(
            Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
            Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    public final MutableObservable<J2DSurface> parent = MutableObservable.ofNullable();

    private final Observable<RootJ2DSurface> root = parent.map(p ->
            p == null ? (RootJ2DSurface) this : p.root.get());

    public abstract Shape shape();

    public J2DSurface() {
        super(J2DNodeHolder.class);
    }

    @Override
    public double devicePixelRatio() {
        return 1;
    }

    @Override
    public CoordinateSpace coordinateSpace() {
        return root.get().coordinateSpaceRoot.origin;
    }

    @Override
    public boolean hitTest(Location point) {
        Vec2 p = point.in(coordinateSpace());
        return shape().contains(p.x(), p.y());
    }

    public static final class ShapeInheritingJ2DSurface extends J2DSurface {

        @Override
        public Shape shape() {
            return parent.get().shape();
        }

        @Override
        public Size size() {
            return parent.get().size();
        }

        @Override
        public boolean hitTest(Location point) {
            return parent.get().hitTest(point);
        }
    }

    public static non-sealed abstract class J2DSurfaceWithOwnShape extends J2DSurface {

        private FillPathRenderNode fillPathRenderNode;

        // Ilyen szándékosan nincs ShapeInheritingJ2DSurface-ben,
        // - group esetén nem jü: két valamit egymásra rajzolni majd clippelni nem ugyanaz,
        //   mint először clippelni majd a clippelteket egymásra rajzolni (antialiasat élek mentén
        //   alpha nem lesz jó).
        // - egyéb effektek esetén se jó: pl. Opacity, ott is hiába ugyanaz a shape,
        //   de nyilván mást kell makeFillRenderNode-nak csinálnia, mint a parentjének  .

        /**
         * ez {@link FillPathRenderNode}-ot ad vissza, de subclassok felülírják, hogy
         * hatékonyabbat adjon vissza
         */
        public RenderNode makeFillRenderNode(Paint paint) {
            if (fillPathRenderNode == null)
                fillPathRenderNode = new FillPathRenderNode();
            fillPathRenderNode.shape.set(shape());
            fillPathRenderNode.paint.set(paint);
            return fillPathRenderNode;
        }
    }

    public static class RootJ2DSurface extends J2DSurfaceWithOwnShape {

        public final Location.CoordinateSpaceRoot coordinateSpaceRoot = new Location.CoordinateSpaceRoot();
        public final MutableObservable<Size> size = MutableObservable.ofNullable();

        @Override
        public Size size() {
            Size size1 = size.get();
            if (size1 == null)
                throw new IllegalStateException();
            return size1;
        }

        @Override
        public Shape shape() {
            Size size = size();
            return new Rectangle2D.Double(0, 0, size.width(), size.height());
        }
    }
}
