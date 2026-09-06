package ui11.platform.opengl.inputtree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.Shape2D;
import ui11.renderer.input.InputNode;

public class OpaqueInputNode extends InputNode {

    public final MutableObservable<Shape2D> shape = MutableObservable.ofNullable();

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        if (shape.get().contains(p.x(), p.y()))
            return pickContext.addResult();
        else
            return false;
    }
}
