package ui11.renderer.j2d.inputtree;

import ui11.geom.Vec2;
import ui11.input.pointer.PointerRegion;
import ui11.observable.MutableObservable;

public class ListenerInputNode extends InputNode {

    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();
    public PointerRegion listener;

    @Override
    public boolean pick(PickContext pickContext, Vec2 p) {
        pickContext.push(this, p);
        boolean result = child.get().pick(pickContext, p);
        pickContext.pop(this);
        return result;
    }
}
