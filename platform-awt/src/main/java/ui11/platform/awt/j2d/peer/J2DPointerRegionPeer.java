package ui11.platform.awt.j2d.peer;

import ui11.EndingWidget;
import ui11.Slot;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DPeerCreationRequest;
import ui11.platform.awt.j2d.inputtree.ListenerInputNode;
import ui11.resolution.PeerCreationRequest;

public class J2DPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

    @Inject private Slot contentSlot;

    @Remember private ListenerInputNode inputNode;

    public J2DPointerRegionPeer(PointerRegion pointerRegion) {
        this.pointerRegion = pointerRegion;
    }

    @Override
    protected void initState() {
        inputNode = new ListenerInputNode();
    }

    @Override
    protected Widget build() {
        J2DNodeHolder childH = makePeer(contentSlot, pointerRegion.content(), new J2DPeerCreationRequest());
        inputNode.child.set(childH.inputNode());
        inputNode.listener = pointerRegion;
        J2DNodeHolder h = new J2DNodeHolder(childH.renderNode(), inputNode);
        return EndingWidget.combine(pointerRegion.content().withSlot(contentSlot), h);
    }
}
