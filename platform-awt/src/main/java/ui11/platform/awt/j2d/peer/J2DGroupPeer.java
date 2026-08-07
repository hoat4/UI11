package ui11.platform.awt.j2d.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DSurface.ShapeInheritingJ2DSurface;
import ui11.platform.awt.j2d.inputtree.*;
import ui11.platform.awt.j2d.rendertree.EmptyRenderNode;
import ui11.platform.awt.j2d.rendertree.GroupRenderNode;
import ui11.platform.awt.j2d.rendertree.RenderNode;
import ui11.provide.Provider;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class J2DGroupPeer extends Widget {

    private final Overlay overlay;
    private final J2DSurface parentSurface;

    @Remember private List<J2DSurface> childSurfaces;
    @Remember private GroupRenderNode groupNode;
    @Remember private GroupInputNode groupInputNode;

    public J2DGroupPeer(Overlay overlay, J2DSurface parentSurface) {
        this.overlay = overlay;
        this.parentSurface = parentSurface;
    }

    @Override
    protected void initState() {
        childSurfaces = new ArrayList<>();
        groupNode = new GroupRenderNode();
        groupInputNode = new GroupInputNode();
    }

    @Override
    protected Widget build() {
        for (int i = 0; i < overlay.items().size(); i++) {
            if (i == childSurfaces.size())
                childSurfaces.add(new ShapeInheritingJ2DSurface());

            J2DSurface surface = childSurfaces.get(i);
            surface.parent.set(parentSurface);
        }

        if (childSurfaces.size() > overlay.items().size())
            childSurfaces.subList(overlay.items().size(), childSurfaces.size()).clear();

        return PeerRequestor.ofMultiple(overlay.items(), childSurfaces, this::doBuild);
    }

    private Widget doBuild(List<? extends PeerRequestor.Result<J2DNodeHolder>> childrenResolutionResults) {
        List<RenderNode> childRenderNodes = new ArrayList<>();
        List<InputNode> childInputNodes = new ArrayList<>();

        Shape shape = parentSurface.shape();

        for (PeerRequestor.Result<J2DNodeHolder> r : childrenResolutionResults) {
            J2DNodeHolder h = r.peer();
            if (!(h.renderNode() instanceof EmptyRenderNode))
                childRenderNodes.add(h.renderNode());
            if (!(h.inputNode() instanceof TransparentInputNode)) {
                if (isOpaque(h.inputNode(), shape))
                    childInputNodes.clear();
                childInputNodes.add(h.inputNode());
            }
        }

        return parentSurface.createResponse(new J2DNodeHolder(
                switch (childRenderNodes.size()) {
                    case 0 -> EmptyRenderNode.INSTANCE;
                    case 1 -> childRenderNodes.getFirst();
                    default -> {
                        groupNode.children.setAll(childRenderNodes);
                        yield groupNode;
                    }
                },
                switch (childInputNodes.size()) {
                    case 0 -> TransparentInputNode.INSTANCE;
                    case 1 -> childInputNodes.getFirst();
                    default -> {
                        groupInputNode.children.setAll(childInputNodes);
                        yield groupInputNode;
                    }
                }
        ));
    }

    private static boolean isOpaque(InputNode node, Shape shape) {
        while (node instanceof ListenerInputNode listenerInputNode)
            node = listenerInputNode.child.get();
        return node instanceof OpaqueInputNode opaqueInputNode && opaqueInputNode.shape.get().equals(shape);
    }
}
