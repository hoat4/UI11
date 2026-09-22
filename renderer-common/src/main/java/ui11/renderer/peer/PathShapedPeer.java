package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Rect;
import ui11.geom.Shape;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.PathShaped;
import ui11.graphics.shaper.RectangleShaped;
import ui11.renderer.layer.Item;
import ui11.renderer.subsurface.ShapedSurface;

public class PathShapedPeer extends Widget {

    private final PathShaped pathShaped;

    @Inject private Surface parentSurface;
    @Inject private Item.ItemRequest request;

    @Remember private ShapedSurface childSurface;

    public PathShapedPeer(PathShaped pathShaped) {
        this.pathShaped = pathShaped;
    }

    @Override
    protected void initState() {
        childSurface = new ShapedSurface();
    }

    @Override
    protected Widget build() {
        Path path = pathShaped.shape();
        childSurface.parent.set(parentSurface);
        childSurface.updateShape(Shape.ofPath(path, parentSurface.coordinateSpace()));
        return ExposeRequest.requestSingle(
                pathShaped.content(),
                new Item.ItemRequest(childSurface),
                peer -> new Expose<>(request, peer, pathShaped.content())
        );
    }
}
