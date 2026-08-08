package ui11.platform.opengl.peer;

import ui11.PeerRequestor;
import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.RectangleShaped;
import ui11.platform.opengl.ClippedSurface;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.Shape2D;
import ui11.provide.Provider;

public class GLRectShapedPeer extends Widget {

    private final RectangleShaped rectShaped;
    private final GLSurface parentSurface;

    @Remember private ClippedSurface childSurface;

    public GLRectShapedPeer(RectangleShaped pathShaped, GLSurface surface) {
        this.rectShaped = pathShaped;
        this.parentSurface = surface;
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
        return PeerRequestor.ofSingle(rectShaped.content(), childSurface, parentSurface::createResponse);
    }
}
