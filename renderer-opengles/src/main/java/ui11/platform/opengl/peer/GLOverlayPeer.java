package ui11.platform.opengl.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.GLNode;
import ui11.platform.opengl.rendertree.GroupNode;

import java.util.ArrayList;
import java.util.List;

public class GLOverlayPeer extends Widget {

    private final Overlay overlay;

    @Inject private Surface surface;
    @Inject private GLVisualContentRequest parentRequest;

    @Remember private GroupNode groupNode;

    public GLOverlayPeer(Overlay overlay) {
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
                new GLVisualContentRequest(surface),
                this::doBuild
        );
    }

    private Widget doBuild(List<? extends GLNode> childrenResolutionResults) {
        List<GLNode> children = new ArrayList<>();

        for (GLNode h : childrenResolutionResults) {
            if (!(h instanceof EmptyNode))
                children.add(h);
        }

        GLNode peer = switch (children.size()) {
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
