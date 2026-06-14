package ui11.platform.opengl.peer;

import ui11.Slot;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.inputtree.ListenerInputNode;
import ui11.resolution.PeerCreationRequest;

public class GLPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

    @Inject private PeerCreationRequest<?> peerCreationRequest;
    @Inject private Slot contentSlot;

    @Remember private ListenerInputNode inputNode;

    public GLPointerRegionPeer(PointerRegion pointerRegion) {
        this.pointerRegion = pointerRegion;
    }

    @Override
    protected void initState() {
        inputNode = new ListenerInputNode();
    }

    @Override
    protected Widget build() {
        if (peerCreationRequest instanceof GLNodeHolder.GLNodeRequest) {
            GLNodeHolder h = makePeer(contentSlot, pointerRegion.content(), new GLNodeHolder.GLNodeRequest());
            inputNode.child.set(h.inputNode());
            inputNode.listener = pointerRegion;
            return new GLNodeHolder(h.renderNode(), inputNode);
        } else
            return pointerRegion.content().withSlot(contentSlot);
    }
}
