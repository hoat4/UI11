package ui11.renderer.subsurface;

import org.jspecify.annotations.NonNull;
import ui11.geom.Location;
import ui11.geom.Mat4;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.observable.MutableObservable;

public class TransformedSurface extends Subsurface {

    public final MutableObservable<Mat4> transformation = MutableObservable.ofNullable();

    @Override
    public Shape layoutShape() {
        return parent().layoutShape().inverseTransform(transformation.get());
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
    public Location.CoordinateSpace coordinateSpace() {
        return parent().coordinateSpace().withTransformation(transformation.get());
    }

    public boolean update(Surface parentSurface, @NonNull Mat4 transformation) {
        parent.set(parentSurface);
        this.transformation.set(transformation);

        Shape parentShape = parent.get().layoutShape(); // TODO többi shape?
        if (Shape.degenerateShape().equals(parentShape))
            return false;

        // TODO nem kéne kiszámolni az inverzt, elég csak tudni hogy létezik
        return transformation.inverseOrNull() != null;
    }

    @Override
    public double devicePixelRatio() {
        return 1;
    }
}
