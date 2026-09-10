package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.input.pointer.PointerRegion;
import ui11.observable.MutableObservable;
import ui11.renderer.input.AbstractPointerListenerNode;
import ui11.renderer.input.PickContext;
import ui11.renderer.j2d.RenderingContext;

public class PointerListenerNode extends J2DNode implements AbstractPointerListenerNode {

    public final MutableObservable<J2DNode> child = MutableObservable.ofNullable();
    public PointerRegion listener;

    @Override
    public void render(RenderingContext ctx) {
        child.get().render(ctx);
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        pickContext.push(this, p.to2D());
        boolean result = child.get().pick(pickContext, p);
        pickContext.pop(this);
        return result;
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
        out.prop("listener", listener);
        out.child("content", child.get());
    }

    @Override
    public PointerRegion listener() {
        return listener;
    }
}
