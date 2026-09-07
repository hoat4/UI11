package ui11.renderer.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Overlay;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.J2DVisualContentRequest.ShapeInheritingJ2DSurface;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.GroupNode;
import ui11.renderer.j2d.rendertree.J2DNode;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class J2DGroupPeer extends Widget {

    private final Overlay overlay;

    @Inject private J2DVisualContentRequest parentSurface;

    @Remember private List<J2DVisualContentRequest> childSurfaces;
    @Remember private GroupNode groupNode;

    public J2DGroupPeer(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    protected void initState() {
        childSurfaces = new ArrayList<>();
        groupNode = new GroupNode();
    }

    @Override
    protected Widget build() {
        for (int i = 0; i < overlay.items().size(); i++) {
            if (i == childSurfaces.size())
                childSurfaces.add(new ShapeInheritingJ2DSurface());

            J2DVisualContentRequest surface = childSurfaces.get(i);
            surface.parent.set(parentSurface);
        }

        if (childSurfaces.size() > overlay.items().size())
            childSurfaces.subList(overlay.items().size(), childSurfaces.size()).clear();

        return PeerRequest.requestMultiple(overlay.items(), childSurfaces, this::doBuild);
    }

    private Widget doBuild(List<? extends J2DNode> childrenResolutionResults) {
        List<J2DNode> children = new ArrayList<>();

        for (J2DNode h : childrenResolutionResults) {
            if (!(h instanceof EmptyNode))
                children.add(h);
        }

        return parentSurface.createResponse(
                switch (children.size()) {
                    case 0 -> EmptyNode.INSTANCE;
                    case 1 -> children.getFirst();
                    default -> {
                        groupNode.children.setAll(children);
                        yield groupNode;
                    }
                }
        );
    }
}
