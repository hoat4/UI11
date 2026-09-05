package ui11.renderer.j2d.rendertree;

import ui11.observable.MutableObservable;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.RenderingContext;

import java.awt.*;

public class FillPathRenderNode extends RenderNode {

    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();
    public final MutableObservable<Paint> paint = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.g.setPaint(paint.get());

        Shape transformedShape = ctx.transform.createTransformedShape(shape.get());
        ctx.g.fill(J2DUtil.intersection(ctx.clip, transformedShape));
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("shape", shape.get());
        out.prop("paint", paint.get());
    }
}
