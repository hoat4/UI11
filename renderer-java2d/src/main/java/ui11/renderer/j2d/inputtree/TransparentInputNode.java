package ui11.renderer.j2d.inputtree;

import ui11.geom.Vec4;
import ui11.renderer.input.InputNode;

public class TransparentInputNode extends InputNode {

    public static final TransparentInputNode INSTANCE = new TransparentInputNode();

    private TransparentInputNode() {
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        return false;
    }
}
