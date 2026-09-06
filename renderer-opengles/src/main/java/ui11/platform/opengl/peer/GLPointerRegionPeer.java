package ui11.platform.opengl.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.renderer.input.ListenerInputNode;

public class GLPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;
    private final GLVisualContentRequest surface;

    @Remember private ListenerInputNode inputNode;

    public GLPointerRegionPeer(PointerRegion pointerRegion, GLVisualContentRequest surface) {
        this.pointerRegion = pointerRegion;
        this.surface = surface;
    }

    @Override
    protected void initState() {
        inputNode = new ListenerInputNode();
    }

    @Override
    protected Widget build() {
        Widget content = pointerRegion.content();
        return PeerRequest.requestSingle(content, surface, result -> {
            inputNode.child.set(result.inputNode());
            inputNode.listener = pointerRegion;
            GLNodeHolder h = new GLNodeHolder(result.renderNode(), inputNode);
            return surface.createResponse(h);
        });
    }
}
