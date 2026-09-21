package ui11.renderer.layer;

import ui11.color.Color;
import ui11.geom.Shape;

public record SolidFillShapeItem(Shape shape, Color color) implements Item {
    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.fill(shape, color);
    }
}
