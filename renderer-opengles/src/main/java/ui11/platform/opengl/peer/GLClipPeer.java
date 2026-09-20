package ui11.platform.opengl.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Clip;
import ui11.platform.opengl.subsurface.ClippedSurface;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.rendertree.ClipNode;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.GLNode;

public class GLClipPeer extends Widget {

    private final Clip clip;
    @Inject private Surface parentSurface;
    @Inject private GLVisualContentRequest parentRequest;

    @Remember private ClipNode clipNode;
    @Remember private ClippedSurface childSurface;

    public GLClipPeer(Clip clip) {
        this.clip = clip;
    }

    @Override
    protected void initState() {
        clipNode = new ClipNode();
        childSurface = new ClippedSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        Widget widget = clip.content();
        return ExposeRequest.requestSingle(widget, new GLVisualContentRequest(childSurface), result -> {
            GLNode peer = makeRenderNode(result, Shape2D.of(
                    childSurface.clipShape(), parentSurface.coordinateSpace()));
            return new Expose<>(parentRequest, peer);
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
