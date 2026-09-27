package ui11.renderer.layer;

public record BlendItem(double opacity, Layer sublayer) implements Item {

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.blend(opacity, sublayer);
    }
}
