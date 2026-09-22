package ui11.renderer.peer;

import ui11.Expose;
import ui11.Widget;
import ui11.renderer.layer.Item;

public class EmptyPeer extends Widget {

    public static final EmptyPeer INSTANCE = new EmptyPeer();

    @Inject private Item.ItemRequest request;

    private EmptyPeer() {
    }

    @Override
    protected Widget build() {
        return new Expose<>(request, Item.EMPTY);
    }
}
