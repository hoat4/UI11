package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.PickContext;
import ui11.renderer.j2d.RenderingContext;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

public class TransformNode extends J2DNode {

    public final MutableObservable<AffineTransform> transformation = MutableObservable.ofNullable();
    public final MutableObservable<J2DNode> child = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.withTransform(transformation.get(), () -> {
            child.get().render(ctx);
        });
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        // inverzt lehet hogy érdemes lenne kiszámítani előre, mert ugyan 2D affin mátrixot könnyű invertálni,
        // de az exception dobása lehet hogy sok idő.

        Point2D transformedPoint;
        try {
            transformedPoint = transformation.get().inverseTransform(new Point2D.Double(p.x(), p.y()), null);
        } catch (NoninvertibleTransformException e) {
            // összelapítja egy 0 méretű területté a transzformáció a child nodeot.
            // ilyenkor nem tudunk kívülről "beletalálni" egérrel.
            return false;
        }

        // TODO
        return child.get().pick(pickContext, new Vec4(transformedPoint.getX(), transformedPoint.getY(), 0, 1));
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("transformation", transformation.get());
        out.child("child", child.get());
    }
}
