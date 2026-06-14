package ui11.platform.opengl.inputtree;

import ui11.geom.Vec4;

public class TransparentInputNode extends InputNode {
    public static final TransparentInputNode INSTANCE = new TransparentInputNode();

    private TransparentInputNode() {
    }

    @Override
    public boolean pick(PickContext pickContext, Vec4 p) {
        return false;
    }
}
