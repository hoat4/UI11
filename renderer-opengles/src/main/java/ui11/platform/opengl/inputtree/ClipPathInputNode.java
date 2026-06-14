package ui11.platform.opengl.inputtree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.Shape2D;

public class ClipPathInputNode extends InputNode {

    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();
    public final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        if (!shape.get().contains(p.x(), p.y()))
            return false;
        return child.get().pick(pickContext, p);
    }
}
