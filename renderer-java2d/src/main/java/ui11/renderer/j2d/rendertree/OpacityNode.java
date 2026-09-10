package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.PickContext;
import ui11.renderer.j2d.RenderingContext;

public class OpacityNode extends J2DNode {

    public final MutableObservable<Double> opacity = MutableObservable.ofNullable();
    public final MutableObservable<J2DNode> content = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.withOpacity(opacity.get(), ()->{
            content.get().render(ctx);
        });
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        return content.get().pick(pickContext, p);
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("opacity", opacity);
        out.child("content", content.get());
    }
}
