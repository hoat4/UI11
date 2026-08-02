package ui11.platform.awt.j2d.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.PathShaped;
import ui11.observable.MutableObservable;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DSurface.J2DSurfaceWithOwnShape;
import ui11.platform.awt.j2d.J2DUtil;
import ui11.provide.Provider;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class J2DPathShapedPeer extends Widget {

    private final PathShaped pathShaped;
    private final J2DSurface parentSurface;

    @Remember private ClippedSurface childSurface;

    public J2DPathShapedPeer(PathShaped pathShaped, J2DSurface surface) {
        this.pathShaped = pathShaped;
        this.parentSurface = surface;
    }

    @Override
    protected void initState() {
        childSurface = new ClippedSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);
        childSurface.updateShape(pathShaped.shape());
        return PeerRequestor.ofSingle(pathShaped.content(), childSurface,
                result -> parentSurface.createResponse(result.peer()));
    }

    private static class ClippedSurface extends J2DSurfaceWithOwnShape {

        private Path shape;

        final MutableObservable<Shape> awtShape = MutableObservable.ofNullable();
        final MutableObservable<Size> size = MutableObservable.ofNullable();

        public void updateShape(Path path) {
            if (path.equals(shape))
                return;

            this.shape = path;
            Shape awtShape = J2DUtil.pathToJ2D(path);
            this.awtShape.set(awtShape);
            //System.err.println("updateShape to "+awtShape);
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
