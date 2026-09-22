package ui11.renderer.layer;

import ui11.color.Color;
import ui11.geom.Shape;
import ui11.geom.Vec2;

import java.util.Collection;
import java.util.List;

public record LinearGradientFillShapeItem(Shape shape, Vec2 direction,
                                          Vec2[] lineStarts, Color[] colors) implements Item {
    @Override
    public Collection<? extends Layer> referredSublayers() {
        return List.of();
    }

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.fillWithLinearGradient(shape, direction, lineStarts, colors);
    }
}
