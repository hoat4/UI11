package ui11.platform.opengl;

import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.observable.MutableObservable;

public class ClippedSurface extends GLVisualContentRequest.GLSurfaceWithOwnShape {

    private final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();
    private final MutableObservable<Size> size = MutableObservable.ofNullable();
    private final MutableObservable<Vec2> renderNodeTranslation = MutableObservable.ofNullable();

    public void updateShape(Shape2D shape, Size size, Vec2 renderNodeTranslation) {
        this.shape.set(shape);
        this.size.set(size);
        this.renderNodeTranslation.set(renderNodeTranslation);
    }

    @Override
    public Shape2D shape() {
        Shape2D shape = this.shape.get();
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

    @Override
    public Vec2 renderNodeTranslation() {
        Vec2 size = this.renderNodeTranslation.get();
        if (size == null)
            throw new IllegalStateException();
        return size;
    }
}