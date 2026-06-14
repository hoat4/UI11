package ui11.platform.opengl;

import ui11.geom.Location;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;

import static ui11.platform.opengl.GLSurface.GLSurfaceWithOwnShape;
import static ui11.platform.opengl.GLSurface.ShapeInheritingGLSurface;

public abstract sealed class GLSurface implements Surface
        permits GLSurfaceWithOwnShape, ShapeInheritingGLSurface {

    public final MutableObservable<GLSurface> parent = MutableObservable.ofNullable();

    private final Observable<RootGLSurface> root = parent.map(p ->
            p == null ? (RootGLSurface) this : p.root.get());

    /**
     * {@link #coordinateSpace() lokális koordinátarendszerben}
     */
    public abstract Shape2D shape();

    /**
     * {@link #coordinateSpace() lokális koordinátarendszerben}
     */
    public abstract Vec2 renderNodeTranslation();

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

    public static final class ShapeInheritingGLSurface extends GLSurface {

        @Override
        public Shape2D shape() {
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

        @Override
        public Vec2 renderNodeTranslation() {
            return parent.get().renderNodeTranslation();
        }
    }

    public static non-sealed abstract class GLSurfaceWithOwnShape extends GLSurface {
        // itt lehetne tárolni shape-függő cacheelt fill nodeot
    }

    public static class RootGLSurface extends GLSurfaceWithOwnShape {

        private final Observable<Vec2> size;
        private final Location.CoordinateSpaceRoot coordinateSpaceRoot = new Location.CoordinateSpaceRoot();

        public RootGLSurface(Observable<Vec2> size) {
            this.size = size;
        }

        @Override
        public Shape2D shape() {
            return new Shape2D.RectShape(Rect.of(size()));
        }

        @Override
        public Vec2 renderNodeTranslation() {
            return Vec2.ZERO;
        }

        @Override
        public Size size() {
            return Size.of(size.get());
        }
    }
}
