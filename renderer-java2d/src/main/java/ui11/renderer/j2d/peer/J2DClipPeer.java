package ui11.renderer.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.graphics.effect.Clip;
import ui11.renderer.j2d.J2DSurface.ClippedJ2DSurface;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.ClipPathNode;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.J2DNode;

public class J2DClipPeer extends Widget {

    private final Clip clip;

    @Inject private Surface parentSurface;
    @Inject private J2DVisualContentRequest parentRequest;

    @Remember private ClipPathNode clipNode;
    @Remember private ClippedJ2DSurface childSurface;

    public J2DClipPeer(Clip clip) {
        this.clip = clip;
    }

    @Override
    protected void initState() {
        clipNode = new ClipPathNode();
        childSurface = new ClippedJ2DSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        return PeerRequest.requestSingle(clip.content(), new J2DVisualContentRequest(childSurface), result -> {
            return parentRequest.createResponse(makeNode(result, childSurface.clipShape()));
            // TODO itt nem kéne a createResponse-ba next-ként visszaadni clip.content()-et?
        });
    }

    private J2DNode makeNode(J2DNode childNode, Shape shape) {
        if (Shape.degenerateShape().equals(shape))
            return EmptyNode.INSTANCE;

        clipNode.content.set(childNode);
        clipNode.shape.set(J2DUtil.shapeToJ2D(shape, parentSurface.coordinateSpace()));
        return clipNode;
    }
}
