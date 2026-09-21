package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Shape;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.RectangleShaped;
import ui11.renderer.layer.Item;
import ui11.renderer.subsurface.ShapedSurface;

public class RectShapedPeer extends Widget {

    private final RectangleShaped rectShaped;

    @Inject private Surface parentSurface;
    @Inject private Item.ItemRequest request;

    @Remember private ui11.renderer.subsurface.ShapedSurface childSurface;

    public RectShapedPeer(RectangleShaped pathShaped) {
        this.rectShaped = pathShaped;
    }

    @Override
    protected void initState() {
        childSurface = new ShapedSurface();
    }

    @Override
    protected Widget build() {
        Size size = rectShaped.shape();
        childSurface.parent.set(parentSurface);
        childSurface.updateShape(Shape.ofRect(Rect.of(size), parentSurface.coordinateSpace()));
        return ExposeRequest.requestSingle(
                rectShaped.content(),
                new Item.ItemRequest(childSurface),
                peer -> new Expose<>(request, peer, rectShaped.content())
        );
    }
}
