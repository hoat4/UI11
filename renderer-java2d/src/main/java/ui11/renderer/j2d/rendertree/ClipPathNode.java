package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.PickContext;
import ui11.renderer.j2d.RenderingContext;

import java.awt.*;

public class ClipPathNode extends J2DNode {

    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();
    public final MutableObservable<J2DNode> content = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        // TODO egymásba ágyazott clippek összeolvasztása, valamint clipbe ágyazott fill esetén nem kell clip

        ctx.withClip(this.shape.get(), () -> {
            this.content.get().render(ctx);
        });
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        if (!shape.get().contains(p.x(), p.y()))
            return false;
        return content.get().pick(pickContext, p);
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("shape", shape.get());
        out.child("content", content.get());
    }
}
