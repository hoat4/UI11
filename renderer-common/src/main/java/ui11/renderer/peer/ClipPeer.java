package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.graphics.effect.Clip;
import ui11.renderer.Renderer;
import ui11.renderer.layer.ClipItem;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.Layer;
import ui11.renderer.subsurface.ClippedSurface;

public class ClipPeer extends Widget {

    private final Clip clip;
    @Inject private Surface parentSurface;
    @Inject private Item.ItemRequest parentRequest;
    @Inject private Renderer<?> renderer;

    @Remember private ClippedSurface childSurface;
    @Remember private Layer childLayer;

    public ClipPeer(Clip clip) {
        this.clip = clip;
    }

    @Override
    protected void initState() {
        childSurface = new ClippedSurface();
    }

    @Override
    protected Widget build() {
        childSurface.parent.set(parentSurface);

        Widget widget = clip.content();
        return ExposeRequest.requestSingle(widget, new Item.ItemRequest(childSurface), result -> {
            Item peer = makeRenderNode(result, childSurface.clipShape());
            return new Expose<>(parentRequest, peer);
        });
    }

    private Item makeRenderNode(Item childNode, Shape clipShape) {
        if (Shape.degenerateShape().equals(clipShape) || childNode == Item.EMPTY) {
            // TODO ilyenkor childLayer-ből valamit fel kéne szabadítani?
            return Item.EMPTY;
        }

        // TODO ha childNode teljesen beleesik awtShapebe, akkor nem kéne ClipNodeot létrehozni
        // TODO ha childben is clip van, akkor a kettőnek a metszetét kéne venni
        childLayer = renderer.createLayer(childLayer);
        childLayer.setContent(childNode);
        return new ClipItem(clipShape, childLayer);
    }
}
