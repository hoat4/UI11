package ui11.renderer.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Clip;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.J2DVisualContentRequest.ShapeInheritingJ2DSurface;
import ui11.renderer.j2d.rendertree.ClipPathNode;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.FillPathNode;
import ui11.renderer.j2d.rendertree.J2DNode;

import java.awt.*;
import java.awt.geom.Area;

public class J2DClipPeer extends Widget {

    private final Clip clip;

    @Inject private J2DVisualContentRequest parentSurface;

    @Remember private ClipPathNode clipNode;
    @Remember private FillPathNode fillPathNode;
    @Remember private J2DVisualContentRequest childSurface;

    public J2DClipPeer(Clip clip) {
        this.clip = clip;
    }

    @Override
    protected void initState() {
        clipNode = new ClipPathNode();
        fillPathNode = new FillPathNode();
        childSurface = new ShapeInheritingJ2DSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        return PeerRequest.requestSingle(clip.content(), childSurface, result -> {
            return parentSurface.createResponse(makeNode(result, childSurface.shape()));
        });
    }

    private J2DNode makeNode(J2DNode childNode, Shape awtShape) {
        if (awtShape == J2DVisualContentRequest.INFINITE_SHAPE)
            return EmptyNode.INSTANCE;

        // TODO ha childNode teljesen beleesik awtShapebe, akkor nem kéne ClipNodeot létrehozni
        switch (childNode) {
            case EmptyNode emptyRenderNode -> {
                return EmptyNode.INSTANCE;
            }
            case FillPathNode childFillPathNode -> {
                fillPathNode.paint.set(childFillPathNode.paint.get());
                fillPathNode.shape.set(intersection(awtShape, childFillPathNode.shape.get()));
                return fillPathNode;
            }
            case ClipPathNode childClipNode -> {
                clipNode.content.set(childClipNode.content.get());
                clipNode.shape.set(intersection(awtShape, childClipNode.shape.get()));
                return clipNode;
            }
            default -> {
                clipNode.content.set(childNode);
                clipNode.shape.set(awtShape);
                return clipNode;
            }
        }
    }

    private static Shape intersection(Shape a, Shape b) {
        if (a.contains(b.getBounds2D()))
            return b;
        else {
            Area area = new Area(a);
            area.intersect(new Area(b));
            return area;
        }
    }
}
