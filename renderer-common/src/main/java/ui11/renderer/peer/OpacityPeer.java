package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Opacity;
import ui11.renderer.Renderer;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.Layer;
import ui11.renderer.layer.BlendItem;

public class OpacityPeer extends Widget {

    private final Opacity opacity;

    @Inject private Renderer<?> renderer;
    @Inject private Surface surface;
    @Inject private Item.ItemRequest parentRequest;

    @Remember private Layer childLayer;

    public OpacityPeer(Opacity opacity) {
        this.opacity = opacity;
    }

    @Override
    protected Widget build() {
        return ExposeRequest.requestSingle(opacity.content(), new Item.ItemRequest(surface), result -> {
            if (!surface.hasVisiblePart())
                // TODO childLayer destroy?
                return new Expose<>(parentRequest, Item.EMPTY);

            childLayer = renderer.createLayer(childLayer);
            childLayer.setContent(result);
            return new Expose<>(parentRequest, new BlendItem(opacity.opacity(), childLayer), opacity.content());
        });
    }
}
