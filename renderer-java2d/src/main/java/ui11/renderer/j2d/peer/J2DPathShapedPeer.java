package ui11.renderer.j2d.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.shaper.PathShaped;
import ui11.renderer.j2d.J2DSurface;
import ui11.renderer.j2d.J2DVisualContentRequest;

public class J2DPathShapedPeer extends Widget {

    private final PathShaped pathShaped;

    @Inject private Surface parentSurface;
    @Inject private J2DVisualContentRequest parentRequest;

    @Remember private J2DSurface.ReshapedJ2DSurface childSurface;

    public J2DPathShapedPeer(PathShaped pathShaped) {
        this.pathShaped = pathShaped;
    }

    @Override
    protected void initState() {
        childSurface = new J2DSurface.ReshapedJ2DSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);
        childSurface.updateShape(pathShaped.shape());
        return ExposeRequest.requestSingle(
                pathShaped.content(),
                new J2DVisualContentRequest(childSurface),
                peer -> new Expose<>(parentRequest, peer)
        );
    }
}
