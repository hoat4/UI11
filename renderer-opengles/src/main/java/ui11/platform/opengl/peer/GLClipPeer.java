package ui11.platform.opengl.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Clip;
import ui11.observable.Observable;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.GLSurface.ShapeInheritingGLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.platform.opengl.inputtree.ClipPathInputNode;
import ui11.platform.opengl.inputtree.InputNode;
import ui11.platform.opengl.inputtree.TransparentInputNode;
import ui11.platform.opengl.rendertree.*;
import ui11.provide.Provider;

public class GLClipPeer extends Widget {

    private final Clip clip;

    @Inject private Observable<Surface> parentSurface;

    @Remember private ClipPathRenderNode clipNode;
    @Remember private ClipPathInputNode clipInputNode;
    @Remember private GLSurface childSurface;

    public GLClipPeer(Clip clip) {
        this.clip = clip;
    }

    @Override
    protected void initState() {
        clipNode = new ClipPathRenderNode();
        clipInputNode = new ClipPathInputNode();
        childSurface = new ShapeInheritingGLSurface();
    }

    @Override
    protected Widget build() {
        GLSurface parentSurface = (GLSurface) this.parentSurface.get();
        childSurface.parent.set(parentSurface);

        Widget widget = clip.content();
        widget = new Provider<>(Surface.class, childSurface, widget);
        return PeerRequestor.ofSingle(widget, new GLNodeHolder.GLNodeRequest(), result -> {
            return new GLNodeHolder(
                    makeRenderNode(result.peer().renderNode(), childSurface.shape()),
                    makeInputNode(result.peer().inputNode(), childSurface.shape())
            );
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
