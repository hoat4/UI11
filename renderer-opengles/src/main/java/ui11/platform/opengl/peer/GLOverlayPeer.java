package ui11.platform.opengl.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.effect.Overlay;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.GLVisualContentRequest.ShapeInheritingGLSurface;
import ui11.platform.opengl.rendertree.EmptyNode;
import ui11.platform.opengl.rendertree.GLNode;
import ui11.platform.opengl.rendertree.GroupNode;

import java.util.ArrayList;
import java.util.List;

public class GLOverlayPeer extends Widget {

    private final Overlay overlay;

    @Inject private GLVisualContentRequest parentSurface;

    @Remember private List<GLVisualContentRequest> childSurfaces;
    @Remember private GroupNode groupNode;

    public GLOverlayPeer(Overlay overlay) {
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
                childSurfaces.add(new ShapeInheritingGLSurface());

            GLVisualContentRequest surface = childSurfaces.get(i);
            surface.parent.set(parentSurface);
        }

        if (childSurfaces.size() > overlay.items().size())
            childSurfaces.subList(overlay.items().size(), childSurfaces.size()).clear();

        return ExposeRequest.requestMultiple(overlay.items(), childSurfaces, this::doBuild);
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
        return new Expose<>(parentSurface, peer);
    }
}
