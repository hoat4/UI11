package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

public class EmptyRenderNode extends RenderNode {

    public static final EmptyRenderNode INSTANCE = new EmptyRenderNode();

    private EmptyRenderNode() {
    }

    @Override
    public void addToDisplayList(Mat4 transform, DisplayList displayList) {
        // nop
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
    }
}
