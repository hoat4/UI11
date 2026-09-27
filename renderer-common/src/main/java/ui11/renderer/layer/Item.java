package ui11.renderer.layer;

import ui11.graphics.Surface;
import ui11.graphics.VisualContentRequest;

public interface Item {

    void visit(Layer.LayerUpdater layerUpdater);

    Item EMPTY = new Item() {

        @Override
        public void visit(Layer.LayerUpdater layerUpdater) {
        }

        @Override
        public String toString() {
            return Item.class.getSimpleName() + ".EMPTY";
        }
    };

    class ItemRequest extends VisualContentRequest<Item> {
        public ItemRequest(Surface surface) {
            super(Item.class, surface);
        }
    }
}
