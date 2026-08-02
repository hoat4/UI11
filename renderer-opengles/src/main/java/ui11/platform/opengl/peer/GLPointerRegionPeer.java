package ui11.platform.opengl.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.inputtree.ListenerInputNode;

public class GLPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;
    private final GLNodeHolder.GLNodeRequest peerReq;

    @Remember private ListenerInputNode inputNode;

    public GLPointerRegionPeer(PointerRegion pointerRegion, GLNodeHolder.GLNodeRequest peerReq) {
        this.pointerRegion = pointerRegion;
        this.peerReq = peerReq;
    }

    @Override
    protected void initState() {
        inputNode = new ListenerInputNode();
    }

    @Override
    protected Widget build() {
        Widget content = pointerRegion.content();
        return PeerRequestor.ofSingle(content, new GLNodeHolder.GLNodeRequest(), result -> {
            inputNode.child.set(result.peer().inputNode());
            inputNode.listener = pointerRegion;
            GLNodeHolder h = new GLNodeHolder(result.peer().renderNode(), inputNode);
            return peerReq.createResponse(h);
        });
    }
}
