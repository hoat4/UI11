package ui11.renderer.j2d;

import org.jspecify.annotations.NonNull;
import ui11.geom.*;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.observable.MutableObservable;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract sealed class J2DSurface implements Surface {

    public final MutableObservable<Surface> parent = MutableObservable.ofNullable();

    public @NonNull Surface parent() {
        Surface p = parent.get();
        if (p == null)
            throw new IllegalStateException("No parent");
        return p;
    }

    @Override
    public Location.CoordinateSpace coordinateSpace() {
        return parent().coordinateSpace();
    }

    @Override
    public Shape clipShape() {
        throw new RuntimeException("TODO");
    }

    @Override
    public Shape inputShape() {
        throw new RuntimeException("TODO");
    }

    @Override
    public double devicePixelRatio() {
        return 1;
    }

    public static final class ReshapedJ2DSurface extends J2DSurface {

        private Path shape;

        final MutableObservable<java.awt.Shape> awtShape = MutableObservable.ofNullable();
        final MutableObservable<Rect> bounds = MutableObservable.ofNullable();

        public void updateShape(Path path) {
            if (path.equals(shape))
                return;

            this.shape = path;
            java.awt.Shape awtShape = J2DUtil.pathToJ2D(path);
            this.awtShape.set(awtShape);
            Rectangle2D bounds = awtShape.getBounds2D();
            this.bounds.set(new Rect(
                    bounds.getMinX(), bounds.getMinY(),
                    bounds.getWidth(), bounds.getHeight()));
        }

        @Override
        public Location.CoordinateSpace coordinateSpace() {
            return parent().coordinateSpace().withTransformation(Mat4.ofTranslation(bounds.get().topLeft()));
        }

        @Override
        public Shape layoutShape() {
            return J2DUtil.shapeFromJ2D(awtShape.get(), parent().coordinateSpace());
        }

        @Override
        public Size size() {
            Rect bounds = this.bounds.get();
            if (bounds == null)
                throw new IllegalStateException();
            return bounds.size();
        }
    }

    public static final class TransformedJ2DSurface extends J2DSurface {

        public final MutableObservable<Mat4> transformation = MutableObservable.ofNullable();

        @Override
        public Shape layoutShape() {
            return parent().layoutShape().inverseTransform(transformation.get());
        }

        @Override
        public Location.CoordinateSpace coordinateSpace() {
            return parent().coordinateSpace().withTransformation(transformation.get());
        }

        public boolean update(Surface parentSurface, @NonNull Mat4 transformation) {
            parent.set(parentSurface);

            Shape parentShape = parent.get().layoutShape(); // TODO többi shape?
            if (Shape.degenerateShape().equals(parentShape))
                return false;

            // TODO nem kéne kiszámolni az inverzt, elég csak tudni hogy létezik
            return transformation.inverseOrNull() != null;
        }
    }

    public static final class StrokeJ2DSurface extends J2DSurface {

        private Path path;

        final MutableObservable<java.awt.Shape> awtShape = MutableObservable.ofNullable();
        final MutableObservable<BasicStroke> stroke = MutableObservable.ofNullable();

        private final MutableObservable<java.awt.Shape> strokedShape = MutableObservable.ofNullable();
        private final MutableObservable<Size> size = MutableObservable.ofNullable();

        public void updateShape(Path path, BasicStroke stroke) {
            java.awt.Shape awtShape = path == this.path ? this.awtShape.snoop() : J2DUtil.pathToJ2D(path);

            if (!awtShape.equals(this.awtShape.get()) || !stroke.equals(this.stroke.get())) {
                this.path = path;
                this.stroke.set(stroke);
                this.awtShape.set(awtShape);
                this.size.set(null);
                this.strokedShape.set(null);

                // TODO ez a nullozgatás felesleges invalidálásokat okoz shape()-nél és size()-nál.
                //      kéne valami API MutableObservable-be, ami megmondja hogy van-e observere éppen.
            }
        }

        @Override
        public Size size() {
            Size size = this.size.get();
            if (size == null) {
                Rectangle2D bounds = awtShape.get().getBounds2D();
                size = new Size(bounds.getMaxX() + stroke.get().getLineWidth(),
                        bounds.getMaxY() + stroke.get().getLineWidth());
                this.size.set(size);
            }
            return size;
        }

        @Override
        public Shape layoutShape() {
            java.awt.Shape strokedShape = strokedShape();
            Path path = J2DUtil.pathFromJ2D(strokedShape);
            return Shape.ofPath(path, parent().coordinateSpace());
        }

        @Override
        public Location.CoordinateSpace coordinateSpace() {
            java.awt.Shape strokedShape = strokedShape();
            Path path = J2DUtil.pathFromJ2D(strokedShape);
            return parent().coordinateSpace().withTransformation(Mat4.ofTranslation(path.bounds().topLeft()));
        }

        private java.awt.Shape strokedShape() {
            java.awt.Shape strokedShape = this.strokedShape.get();
            if (strokedShape == null) {
                strokedShape = stroke.get().createStrokedShape(this.awtShape.get());
                this.strokedShape.set(strokedShape);
            }
            return strokedShape;
        }
    }

    public static final class ClippedJ2DSurface extends J2DSurface {

        @Override
        public Shape layoutShape() {
            return parent().layoutShape();
        }

        @Override
        public Shape clipShape() {
            // TODO itt valahogy mergeölni kéne a clippeket
            throw new RuntimeException("TODO");
        }

        @Override
        public Shape inputShape() {
            throw new RuntimeException("TODO");
        }
    }
}
