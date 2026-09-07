package ui11.renderer.j2d.rendertree;

import ui11.geom.Vec4;
import ui11.renderer.input.InputNode;
import ui11.renderer.j2d.RenderingContext;

public class EmptyNode extends J2DNode {

    public static final EmptyNode INSTANCE = new EmptyNode();

    private EmptyNode() {
    }

    @Override
    public void render(RenderingContext ctx) {
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
