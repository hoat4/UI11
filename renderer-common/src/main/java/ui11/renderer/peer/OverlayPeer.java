package ui11.renderer.peer;

import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.effect.Overlay;
import ui11.renderer.layer.GroupItem;
import ui11.renderer.layer.Item;

import java.util.List;

public class OverlayPeer extends Widget {

    private final Overlay overlay;

    @Inject private Surface surface;
    @Inject private Item.ItemRequest parentRequest;

    public OverlayPeer(Overlay overlay) {
        this.overlay = overlay;
    }

    @Override
    protected Widget build() {
        return ExposeRequest.requestOnMultipleWidgets(
                overlay.items(),
                new Item.ItemRequest(surface),
                this::doBuild
        );
    }

    private Widget doBuild(List<? extends Item> childrenResolutionResults) {
        return new Expose<>(parentRequest, GroupItem.of(childrenResolutionResults));
    }
}
