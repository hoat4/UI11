package ui11.renderer.j2d.inputtree;

import ui11.geom.Vec2;
import ui11.observable.MutableObservable;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public class TransformInputNode extends InputNode {

    public final MutableObservable<AffineTransform> transformation = MutableObservable.ofNullable();
    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();

    @Override
    public boolean pick(PickContext pickContext, Vec2 p) {
        // inverzt lehet hogy érdemes lenne kiszámítani előre, mert ugyan 2D affin mátrixot könnyű invertálni,
        // de az exception dobása lehet hogy sok idő.

        Point2D transformedPoint;
        try {
            transformedPoint = transformation.get().inverseTransform(new Double(p.x(), p.y()), null);
        } catch (NoninvertibleTransformException e) {
            // összelapítja egy 0 méretű területté a transzformáció a child nodeot.
            // ilyenkor nem tudunk kívülről "beletalálni" egérrel.
            return false;
        }

        return child.get().pick(pickContext, new Vec2(transformedPoint.getX(), transformedPoint.getY()));
    }
}
