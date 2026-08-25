package ui11.platform.awt.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Clip;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DSurface.ShapeInheritingJ2DSurface;
import ui11.platform.awt.j2d.inputtree.ClipPathInputNode;
import ui11.platform.awt.j2d.inputtree.InputNode;
import ui11.platform.awt.j2d.inputtree.TransparentInputNode;
import ui11.platform.awt.j2d.rendertree.ClipPathRenderNode;
import ui11.platform.awt.j2d.rendertree.EmptyRenderNode;
import ui11.platform.awt.j2d.rendertree.FillPathRenderNode;
import ui11.platform.awt.j2d.rendertree.RenderNode;

import java.awt.*;
import java.awt.geom.Area;

public class J2DClipPeer extends Widget {

    private final Clip clip;

    @Inject private J2DSurface parentSurface;

    @Remember private ClipPathRenderNode clipNode;
    @Remember private FillPathRenderNode fillPathNode;
    @Remember private ClipPathInputNode clipInputNode;
    @Remember private J2DSurface childSurface;

    public J2DClipPeer(Clip clip) {
        this.clip = clip;
    }

    @Override
    protected void initState() {
        clipNode = new ClipPathRenderNode();
        fillPathNode = new FillPathRenderNode();
        clipInputNode = new ClipPathInputNode();
        childSurface = new ShapeInheritingJ2DSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        return PeerRequest.requestSingle(clip.content(), childSurface, result -> {
            return new J2DNodeHolder(
                    makeRenderNode(result.renderNode(), childSurface.shape()),
                    makeInputNode(result.inputNode(), childSurface.shape())
            );
        });
    }

    private RenderNode makeRenderNode(RenderNode childNode, Shape awtShape) {
        if (awtShape == J2DSurface.INFINITE_SHAPE)
            return EmptyRenderNode.INSTANCE;

        // TODO ha childNode teljesen beleesik awtShapebe, akkor nem kéne ClipNodeot létrehozni
        switch (childNode) {
            case EmptyRenderNode emptyRenderNode -> {
                return EmptyRenderNode.INSTANCE;
            }
            case FillPathRenderNode childFillPathNode -> {
                fillPathNode.paint.set(childFillPathNode.paint.get());
                fillPathNode.shape.set(intersection(awtShape, childFillPathNode.shape.get()));
                return fillPathNode;
            }
            case ClipPathRenderNode childClipNode -> {
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

    private InputNode makeInputNode(InputNode childNode, Shape awtShape) {
        if (awtShape == J2DSurface.INFINITE_SHAPE)
            return TransparentInputNode.INSTANCE;

        if (childNode == TransparentInputNode.INSTANCE)
            return childNode;
        else {
            clipInputNode.child.set(childNode);
            clipInputNode.shape.set(awtShape);
            return clipInputNode;
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
