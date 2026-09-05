package ui11.renderer.j2d.inputtree;

import ui11.geom.Vec2;

public class TransparentInputNode extends InputNode {
    public static final TransparentInputNode INSTANCE = new TransparentInputNode();

    private TransparentInputNode() {
    }

    @Override
    public boolean pick(PickContext pickContext, Vec2 p) {
        return false;
    }
}
