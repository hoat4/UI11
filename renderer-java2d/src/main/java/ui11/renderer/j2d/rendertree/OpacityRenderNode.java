package ui11.renderer.j2d.rendertree;

import ui11.observable.MutableObservable;
import ui11.renderer.j2d.RenderingContext;

public class OpacityRenderNode extends RenderNode {

    public final MutableObservable<Double> opacity = MutableObservable.ofNullable();
    public final MutableObservable<RenderNode> content = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.withOpacity(opacity.get(), ()->{
            content.get().render(ctx);
        });
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("opacity", opacity);
        out.child("content", content.get());
    }
}
