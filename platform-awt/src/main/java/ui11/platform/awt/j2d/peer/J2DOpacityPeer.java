package ui11.platform.awt.j2d.peer;

import ui11.Slot;
import ui11.Widget;
import ui11.graphics.effect.Opacity;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DPeerCreationRequest;
import ui11.platform.awt.j2d.rendertree.OpacityRenderNode;

public class J2DOpacityPeer extends Widget {

    private final Opacity opacity;

    @Inject private Slot childSlot;

    @Remember private OpacityRenderNode opacityRenderNode;

    public J2DOpacityPeer(Opacity opacity) {
        this.opacity = opacity;
    }

    @Override
    protected void initState() {
        opacityRenderNode = new OpacityRenderNode();
    }

    @Override
    protected Widget build() {
        J2DNodeHolder childNodeHolder = makePeer(childSlot, opacity.content(), new J2DPeerCreationRequest());
        opacityRenderNode.opacity.set(opacity.opacity());
        opacityRenderNode.content.set(childNodeHolder.renderNode());
        return new J2DNodeHolder(
                opacityRenderNode,
                childNodeHolder.inputNode()
        );
    }
}
