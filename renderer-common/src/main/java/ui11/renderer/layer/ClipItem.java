package ui11.renderer.layer;

import ui11.geom.Shape;

public record ClipItem(Shape shape, Layer sublayer) implements Item {

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.fill(shape, sublayer);
    }
}
