package ui11.platform.awt.j2d.rendertree;

import ui11.platform.awt.j2d.RenderingContext;

public class EmptyRenderNode extends RenderNode {

    public static final EmptyRenderNode INSTANCE = new EmptyRenderNode();

    private EmptyRenderNode() {
    }

    @Override
    public void render(RenderingContext ctx) {
        // nop
    }

    @Override
    public void debugPrint(RenderTreePrinter out) {
    }
}
