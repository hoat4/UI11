package ui11.platform.awt.j2d.peer;

import ui11.Slot;
import ui11.Widget;
import ui11.graphics.effect.Opacity;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DPeerCreationRequest;
import ui11.platform.awt.j2d.rendertree.OpacityRenderNode;

public class J2DOpacityPeer extends Widget {

    private final Opacity opacity;

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
        return new J2DPeerCreationRequest().executedOn(opacity.content(), result -> {
            opacityRenderNode.opacity.set(opacity.opacity());
            opacityRenderNode.content.set(result.peer().renderNode());
            return new J2DNodeHolder(
                    opacityRenderNode,
                    result.peer().inputNode()
            );
        });
    }
}
