package ui11.platform.opengl.subsurface;

import ui11.geom.*;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.Shape2D;
import ui11.renderer.Subsurface;

public class ShapedSurface extends Subsurface {

    final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();
    final MutableObservable<Rect> bounds = MutableObservable.ofNullable();

    public void updateShape(Shape2D shape) {
        this.shape.set(shape);
        this.bounds.set(shape.bounds());
    }

    @Override
    public Location.CoordinateSpace coordinateSpace() {
        return parent().coordinateSpace().withTransformation(Mat4.ofTranslation(bounds.get().topLeft()));
    }

    @Override
    public Shape layoutShape() {
        return new Shape2D.Shape2DAsShape(shape.get(), parent().coordinateSpace());
    }

    @Override
    public Size size() {
        Rect bounds = this.bounds.get();
        if (bounds == null)
            throw new IllegalStateException();
        return bounds.size();
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
}
