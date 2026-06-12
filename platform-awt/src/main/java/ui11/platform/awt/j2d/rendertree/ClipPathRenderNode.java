package ui11.platform.awt.j2d.rendertree;

import ui11.observable.MutableObservable;
import ui11.platform.awt.j2d.RenderingContext;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ClipPathRenderNode extends RenderNode {

    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();
    public final MutableObservable<RenderNode> content = MutableObservable.ofNullable();

    @Override
    public void render(RenderingContext ctx) {
        ctx.withClip(this.shape.get(), () -> {
            this.content.get().render(ctx);
        });
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("shape", shape.get());
        out.child("content", content.get());
    }
}
