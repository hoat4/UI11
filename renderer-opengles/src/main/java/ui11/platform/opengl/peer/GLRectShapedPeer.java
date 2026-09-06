package ui11.platform.opengl.peer;

import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.ClippedSurface;
import ui11.platform.opengl.GLVisualContentRequest;
import ui11.platform.opengl.Shape2D;

public class GLRectShapedPeer extends Widget {

    private final RectangleShaped rectShaped;

    @Inject private GLVisualContentRequest parentSurface;

    @Remember private ClippedSurface childSurface;

    public GLRectShapedPeer(RectangleShaped pathShaped) {
        this.rectShaped = pathShaped;
    }

    @Override
    protected void initState() {
        childSurface = new ClippedSurface();
    }

    @Override
    protected Widget build() {
        Size size = rectShaped.shape();
        childSurface.parent.set(parentSurface);
        childSurface.updateShape(new Shape2D.RectShape(Rect.of(size)), size, parentSurface.renderNodeTranslation());
        return PeerRequest.requestSingle(rectShaped.content(), childSurface, parentSurface::createResponse);
    }
}
