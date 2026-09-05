package ui11.renderer.j2d.rendertree;

import ui11.observable.MutableObservable;
import ui11.renderer.j2d.RenderingContext;

import java.awt.geom.AffineTransform;

public class TransformRenderNode extends RenderNode {

    public final MutableObservable<AffineTransform> transformation = MutableObservable.ofNullable();
    public final MutableObservable<RenderNode> child = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.withTransform(transformation.get(), () -> {
            child.get().render(ctx);
        });
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("transformation", transformation.get());
        out.child("child", child.get());
    }
}
