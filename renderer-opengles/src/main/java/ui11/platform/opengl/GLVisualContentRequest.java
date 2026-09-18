package ui11.platform.opengl;

import ui11.graphics.Surface;
import ui11.graphics.VisualContentRequest;
import ui11.platform.opengl.rendertree.GLNode;

public final class GLVisualContentRequest extends VisualContentRequest<GLNode> {

    public GLVisualContentRequest(Surface surface) {
        super(GLNode.class, surface);
    }
}
