package ui11.platform.awt.j2d.inputtree;

import ui11.geom.Vec2;
import ui11.observable.MutableObservable;

import java.awt.*;

public class ClipPathInputNode extends InputNode {

    public final MutableObservable<InputNode> child = MutableObservable.ofNullable();
    public final MutableObservable<Shape> shape = MutableObservable.ofNullable();

    @Override
    public boolean pick(PickContext pickContext, Vec2 p) {
        if (!shape.get().contains(p.x(), p.y()))
            return false;
        return child.get().pick(pickContext, p);
    }
}
