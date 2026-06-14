package ui11.platform.opengl.inputtree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.Shape2D;


public class StrokeInputNode extends InputNode {

    // TODO egyéb stroke beállítások: join, cap, stb.

    public final MutableObservable<Double> thickness = MutableObservable.ofNullable();
    public final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();
    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();

    private Shape2D strokedShape;
    private Shape2D shapeForStrokedShape;
    private double thicknessForStrokedShape = Double.NaN;

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        Shape2D shape = this.shape.get();
        double thickness = this.thickness.get();

        if (shape != shapeForStrokedShape || thickness != thicknessForStrokedShape) {
            strokedShape = shape.createStrokedShape(thickness);
            shapeForStrokedShape = shape;
            thicknessForStrokedShape = thickness;
        }

        if (strokedShape.contains(p.x(), p.y()))
            return child.get().pick(pickContext, p);
        else
            return false;
    }
}
