package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec2;
import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.PickContext;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.RenderingContext;

import java.awt.*;

public class FillPathNode extends J2DNode {

    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();
    public final MutableObservable<Paint> paint = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.g.setPaint(paint.get());

        Shape transformedShape = ctx.transform.createTransformedShape(shape.get());
        ctx.g.fill(J2DUtil.intersection(ctx.clip, transformedShape));
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        Vec2 p2d = p.to2D();
        if (shape.get().contains(p2d.x(), p2d.y()))
            return pickContext.addResult();
        else
            return false;
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("shape", shape.get());
        out.prop("paint", paint.get());
    }
}
