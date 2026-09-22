package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.geom.Path;
import ui11.graphics.Surface;
import ui11.graphics.shaper.Stroke;
import ui11.renderer.layer.Item;
import ui11.renderer.subsurface.ShapedSurface;
import ui11.renderer.subsurface.StrokedShape;
import ui11.text.TextStyle;

public class StrokePeer extends Widget {

    private final Stroke stroke;

    @Inject private Surface parentSurface;
    @Inject private Item.ItemRequest request;
    @Inject private TextStyle textStyle;

    @Remember private ShapedSurface childSurface;

    public StrokePeer(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    protected void initState() {
        childSurface = new ShapedSurface();
    }

    @Override
    protected Widget build() {
        double thickness = stroke.thickness().px() + stroke.thickness().em() * textStyle.size();
        // relative része nincs a thicknessnek, ld. Stroke konstruktorachildSurface.parent.set(parentSurface);
        Path path = stroke.path();
        childSurface.updateShape(new StrokedShape(path, thickness, parentSurface.coordinateSpace()));
        return ExposeRequest.requestSingle(
                stroke.texture(),
                new Item.ItemRequest(childSurface),
                peer -> new Expose<>(request, peer, stroke.texture())
        );
    }
}
