package ui11.renderer.j2d;

import ui11.graphics.Surface;
import ui11.graphics.VisualContentRequest;
import ui11.renderer.j2d.rendertree.J2DNode;

public class J2DVisualContentRequest extends VisualContentRequest<J2DNode> {

    public J2DVisualContentRequest(Surface surface) {
        super(J2DNode.class, surface);
    }
}
