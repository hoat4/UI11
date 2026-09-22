package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.input.pointer.PointerRegion;
import ui11.renderer.Renderer;
import ui11.renderer.layer.BlendItem;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.Layer;

public class PointerRegionPeer extends Widget {

    private final PointerRegion pointerRegion;

    @Inject private Renderer<?> renderer;
    @Inject private Item.ItemRequest parentRequest;
    @Inject private Surface surface;

    @Remember private Layer childLayer;

    public PointerRegionPeer(PointerRegion pointerRegion) {
        this.pointerRegion = pointerRegion;
    }

    @Override
    protected Widget build() {
        Widget content = pointerRegion.content();
        return ExposeRequest.requestSingle(content, new Item.ItemRequest(surface), result -> {
            // TODO if (!surface.hasNoVisibleInputPart()) ???

            childLayer = renderer.createLayer(childLayer);
            childLayer.setContent(result);
            childLayer.pointerListener = pointerRegion;
            return new Expose<>(parentRequest, new BlendItem(1, childLayer), content);
        });
    }
}
