package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.geom.Vec4;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.renderer.input.InputNode;

public class EmptyNode extends GLNode {

    public static final EmptyNode INSTANCE = new EmptyNode();

    private EmptyNode() {
    }

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        // nop
    }

    @Override
    public boolean pick(InputNode.PickContext pickContext, Vec4 p) {
        return false;
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
    }
}
