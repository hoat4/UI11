/*
package ui11.platform.opengl.peer;

import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.PathShaped;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.GLSurface.GLSurfaceWithOwnShape;
import ui11.platform.opengl.J2DUtil;
import ui11.provide.Provider;


import java.awt.geom.Rectangle2D;

public class GLPathShapedPeer extends Widget {

    private final PathShaped pathShaped;

    @Inject private Observable<Surface> parentSurface;

    @State private ClippedSurface childSurface;

    public GLPathShapedPeer(PathShaped pathShaped) {
        this.pathShaped = pathShaped;
    }

    @Override
    protected void initState() {
        childSurface = new ClippedSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set((GLSurface) parentSurface.get());
        childSurface.updateShape(pathShaped.shape());
        return new Provider<>(Surface.class, childSurface, pathShaped.content());
    }

    private static class ClippedSurface extends GLSurfaceWithOwnShape {

        private Path shape;

        final MutableObservable<Shape> awtShape = MutableObservable.ofNullable();
        final MutableObservable<Size> size = MutableObservable.ofNullable();

        public void updateShape(Path path) {
            if (path.equals(shape))
                return;

            this.shape = path;
            Shape awtShape = J2DUtil.pathToJ2D(path);
            this.awtShape.set(awtShape);
            Rectangle2D bounds = awtShape.getBounds2D();
            this.size.set(new Size(bounds.getMaxX(), bounds.getMaxY()));
        }

        @Override
        public Shape shape() {
            Shape shape = awtShape.get();
            if (shape == null)
                throw new IllegalStateException();
            return shape;
        }

        @Override
        public Size size() {
            Size size = this.size.get();
            if (size == null)
                throw new IllegalStateException();
            return size;
        }
    }
}
*/