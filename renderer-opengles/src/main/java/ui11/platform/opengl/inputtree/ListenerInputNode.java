package ui11.platform.opengl.inputtree;

import ui11.geom.Vec4;
import ui11.input.pointer.PointerRegion;
import ui11.observable.MutableObservable;

public class ListenerInputNode extends InputNode {

    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();
    public PointerRegion listener;

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        pickContext.push(this, p.to2D());
        boolean result = child.get().pick(pickContext, p);
        pickContext.pop(this);
        return result;
    }
}
