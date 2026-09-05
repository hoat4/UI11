package ui11.renderer.j2d.inputtree;

import ui11.geom.Vec2;
import ui11.observable.MutableObservable;

import java.awt.*;

public class StrokeInputNode extends InputNode {

    public final MutableObservable<BasicStroke> stroke = MutableObservable.ofNullable();
    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();
    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();

    private Shape strokedShape;
    private Shape shapeForStrokedShape;
    private BasicStroke strokeForStrokedShape;

    @Override
    public boolean pick(PickContext pickContext, Vec2 p) {
        Shape shape = this.shape.get();
        BasicStroke stroke = this.stroke.get();

        if (shape != shapeForStrokedShape || !stroke.equals(strokeForStrokedShape)) {
            strokedShape = stroke.createStrokedShape(shape);
            shapeForStrokedShape = shape;
            strokeForStrokedShape = stroke;
        }

        if (strokedShape.contains(p.x(), p.y()))
            return child.get().pick(pickContext, p);
        else
            return false;
    }
}
