package ui11.renderer.subsurface;

import ui11.geom.*;
import ui11.observable.MutableObservable;

public class ShapedSurface extends Subsurface {

    private final MutableObservable<Shape> shape = MutableObservable.ofNullable();

    public void updateShape(Shape shape) {
        this.shape.set(shape);
    }

    @Override
    public Location.CoordinateSpace coordinateSpace() {
        Location.CoordinateSpace cs = parent().coordinateSpace();
        cs = cs.withTransformation(Mat4.ofTranslation(layoutShape().bounds(cs).topLeft()));
        return cs;
    }

    @Override
    public Shape layoutShape() {
        Shape s = shape.get();
        if (s == null)
            throw new IllegalStateException();
        return s;
    }

    @Override
    public Size size() {
        return layoutShape().bounds(coordinateSpace()).size();
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
