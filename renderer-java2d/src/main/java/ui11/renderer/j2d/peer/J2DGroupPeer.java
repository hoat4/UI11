package ui11.renderer.j2d.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.GroupNode;
import ui11.renderer.j2d.rendertree.J2DNode;

import java.util.ArrayList;
import java.util.List;

public class J2DGroupPeer extends Widget {

    private final Overlay overlay;

    @Inject private Surface surface;
    @Inject private J2DVisualContentRequest parentRequest;

    @Remember private GroupNode groupNode;

    public J2DGroupPeer(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    protected void initState() {
        groupNode = new GroupNode();
    }

    @Override
    protected Widget build() {
        return ExposeRequest.requestOnMultipleWidgets(
                overlay.items(),
                new J2DVisualContentRequest(surface),
                this::doBuild
        );
    }

    private Widget doBuild(List<? extends J2DNode> childrenResolutionResults) {
        List<J2DNode> children = new ArrayList<>();

        for (J2DNode h : childrenResolutionResults) {
            if (!(h instanceof EmptyNode))
                children.add(h);
        }

        J2DNode peer = switch (children.size()) {
            case 0 -> EmptyNode.INSTANCE;
            case 1 -> children.getFirst();
            default -> {
                groupNode.children.setAll(children);
                yield groupNode;
            }
        };
        return new Expose<>(parentRequest, peer);
    }
}
