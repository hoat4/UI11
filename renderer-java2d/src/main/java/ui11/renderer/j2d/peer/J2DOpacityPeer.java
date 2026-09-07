package ui11.renderer.j2d.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.graphics.effect.Opacity;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.rendertree.OpacityNode;

public class J2DOpacityPeer extends Widget {

    private final Opacity opacity;

    @Inject private J2DVisualContentRequest surface;

    @Remember private OpacityNode opacityNode;

    public J2DOpacityPeer(Opacity opacity) {
        this.opacity = opacity;
    }

    @Override
    protected void initState() {
        opacityNode = new OpacityNode();
    }

    @Override
    protected Widget build() {
        return PeerRequest.requestSingle(opacity.content(), surface, result -> {
            opacityNode.opacity.set(opacity.opacity());
            opacityNode.content.set(result);
            return surface.createResponse(opacityNode);
        });
    }
}
