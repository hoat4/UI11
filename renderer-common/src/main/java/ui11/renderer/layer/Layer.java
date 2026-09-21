package ui11.renderer.layer;

import ui11.color.Color;
import ui11.geom.Shape;
import ui11.geom.Vec2;

import java.util.ArrayList;
import java.util.List;

public abstract class Layer {

    public void setContent(Item item) {
        class Cursor {
            final List<? extends Item> items;
            int i;

            Cursor(List<? extends Item> items) {
                this.items = items;
            }
        }

        List<Cursor> stack = new ArrayList<>();
        stack.add(new Cursor(List.of(item)));
        LayerUpdater layerUpdater = createLayerUpdater();
        while (!stack.isEmpty()) {
            Cursor cursor = stack.getLast();
            if (cursor.i == cursor.items.size()) {
                stack.removeLast();
                continue;
            }
            item = cursor.items.get(cursor.i++);
            if (item instanceof GroupItem groupItem)
                stack.add(new Cursor(groupItem.items()));
            else
                item.visit(layerUpdater);
        }
        layerUpdater.finish();
    }

    public abstract LayerUpdater createLayerUpdater();

    // végén csak kijön egy Graphics-szerű API valahol
    public interface LayerUpdater {

        void fill(Shape shape, Color color);

        void fillWithLinearGradient(Shape shape, Vec2 direction,
                                    Vec2[] lineStarts, Color[] colors);

        void fill(Shape shape, Layer sublayer);

        void finish();
    }
}
