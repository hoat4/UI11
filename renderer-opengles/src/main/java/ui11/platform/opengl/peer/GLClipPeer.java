package ui11.platform.opengl.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Clip;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.GLVisualContentRequest.ShapeInheritingGLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.inputtree.ClipPathInputNode;
import ui11.platform.opengl.inputtree.InputNode;
import ui11.platform.opengl.inputtree.TransparentInputNode;
import ui11.platform.opengl.rendertree.*;

public class GLClipPeer extends Widget {

    private final Clip clip;
    private final GLVisualContentRequest parentSurface;

    @Remember private ClipPathRenderNode clipNode;
    @Remember private ClipPathInputNode clipInputNode;
    @Remember private GLVisualContentRequest childSurface;

    public GLClipPeer(Clip clip, GLVisualContentRequest parentSurface) {
        this.clip = clip;
        this.parentSurface = parentSurface;
    }

    @Override
    protected void initState() {
        clipNode = new ClipPathRenderNode();
        clipInputNode = new ClipPathInputNode();
        childSurface = new ShapeInheritingGLSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        Widget widget = clip.content();
        return PeerRequest.requestSingle(widget, childSurface, result -> {
            return parentSurface.createResponse(new GLNodeHolder(
                    makeRenderNode(result.renderNode(), childSurface.shape()),
                    makeInputNode(result.inputNode(), childSurface.shape())
            ));
        });
    }

    private RenderNode makeRenderNode(RenderNode childNode, Shape2D parentShape) {
        if (parentShape == Shape2D.InfinitePlane.INFINITE_PLANE)
            return EmptyRenderNode.INSTANCE;

        // TODO ha childNode teljesen beleesik awtShapebe, akkor nem kéne ClipNodeot létrehozni
        switch (childNode) {
            case EmptyRenderNode emptyRenderNode -> {
                return EmptyRenderNode.INSTANCE;
            }
            case ClipPathRenderNode childClipNode -> {
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

    private InputNode makeInputNode(InputNode childNode, Shape2D parentShape) {
        if (parentShape == Shape2D.InfinitePlane.INFINITE_PLANE)
            return TransparentInputNode.INSTANCE;

        if (childNode == TransparentInputNode.INSTANCE)
            return childNode;
        else {
            clipInputNode.child.set(childNode);
            clipInputNode.shape.set(parentShape);
            return clipInputNode;
        }
    }
}
