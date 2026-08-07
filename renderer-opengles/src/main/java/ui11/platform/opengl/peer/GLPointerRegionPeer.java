package ui11.platform.opengl.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.inputtree.ListenerInputNode;

public class GLPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;
    private final GLSurface surface;

    @Remember private ListenerInputNode inputNode;

    public GLPointerRegionPeer(PointerRegion pointerRegion, GLSurface surface) {
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
        return PeerRequestor.ofSingle(content, surface, result -> {
            inputNode.child.set(result.peer().inputNode());
            inputNode.listener = pointerRegion;
            GLNodeHolder h = new GLNodeHolder(result.peer().renderNode(), inputNode);
            return surface.createResponse(h);
        });
    }
}
