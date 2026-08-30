package ui11.platform.awt.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Opacity;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.rendertree.OpacityRenderNode;

public class J2DOpacityPeer extends Widget {

    private final Opacity opacity;

    @Inject private J2DSurface surface;

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
        return PeerRequest.requestSingle(opacity.content(), surface, result -> {
            opacityRenderNode.opacity.set(opacity.opacity());
            opacityRenderNode.content.set(result.renderNode());
            return surface.createResponse(new J2DNodeHolder(
                    opacityRenderNode,
                    result.inputNode()
            ));
        });
    }
}
