package ui11.renderer.layer;

import java.util.ArrayList;
import java.util.List;

public record GroupItem(List<? extends Item> items) implements Item {

    public static Item of(List<? extends Item> items) {
        List<Item> children = new ArrayList<>();

        for (Item h : items) {
            if (h != Item.EMPTY)
                children.add(h);
        }

        return switch (children.size()) {
            case 0 -> Item.EMPTY;
            case 1 -> children.getFirst();
            default -> new GroupItem(children);
        };
    }

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        // special cased in Layer to prevent stack overflow caused by too deep Item hierarchies
        throw new RuntimeException("should not reach here");
    }
}
