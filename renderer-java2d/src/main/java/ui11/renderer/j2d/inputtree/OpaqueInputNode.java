package ui11.renderer.j2d.inputtree;

import ui11.geom.Vec4;
import ui11.observable.MutableObservable;
import ui11.renderer.input.InputNode;

import java.awt.*;

public class OpaqueInputNode extends InputNode {

    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        if (shape.get().contains(p.x(), p.y()))
            return pickContext.addResult();
        else
            return false;
    }
}
