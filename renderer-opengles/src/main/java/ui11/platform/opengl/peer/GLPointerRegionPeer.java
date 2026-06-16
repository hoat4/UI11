package ui11.platform.opengl.peer;

import ui11.EndingWidget;
import ui11.Slot;
import ui11.Widget;
import ui11.input.pointer.PointerRegion;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.inputtree.ListenerInputNode;
import ui11.resolution.PeerCreationRequest;

public class GLPointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

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
        Widget content = pointerRegion.content().withSlot(contentSlot);
        return new GLNodeHolder.GLNodeRequest().executedOn(content, peer->{
            inputNode.child.set(peer.inputNode());
            inputNode.listener = pointerRegion;
            GLNodeHolder h = new GLNodeHolder(peer.renderNode(), inputNode);
            return EndingWidget.combine(content, h);
        });
    }
}
