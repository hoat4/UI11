package ui11.platform.opengl.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Clip;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.GLVisualContentRequest.ShapeInheritingGLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.rendertree.*;

public class GLClipPeer extends Widget {

    private final Clip clip;
    private final GLVisualContentRequest parentSurface;

    @Remember private ClipNode clipNode;
    @Remember private GLVisualContentRequest childSurface;

    public GLClipPeer(Clip clip, GLVisualContentRequest parentSurface) {
        this.clip = clip;
        this.parentSurface = parentSurface;
    }

    @Override
    protected void initState() {
        clipNode = new ClipNode();
        childSurface = new ShapeInheritingGLSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        Widget widget = clip.content();
        return PeerRequest.requestSingle(widget, childSurface, result -> {
            return parentSurface.createResponse(
                    makeRenderNode(result, childSurface.shape())
            );
        });
    }

    private GLNode makeRenderNode(GLNode childNode, Shape2D parentShape) {
        if (parentShape == Shape2D.InfinitePlane.INFINITE_PLANE)
            return EmptyNode.INSTANCE;

        // TODO ha childNode teljesen beleesik awtShapebe, akkor nem kéne ClipNodeot létrehozni
        switch (childNode) {
            case EmptyNode emptyRenderNode -> {
                return EmptyNode.INSTANCE;
            }
            case ClipNode childClipNode -> {
                clipNode.content.set(childClipNode.content.get());
                clipNode.shape.set(Shape2D.intersection(parentShape, childClipNode.shape.get()));
                return clipNode;
            }
            default -> {
                clipNode.content.set(childNode);
                clipNode.shape.set(parentShape);
                return clipNode;
            }
        }
    }
}
