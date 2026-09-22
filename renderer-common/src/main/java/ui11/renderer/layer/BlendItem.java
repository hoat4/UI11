package ui11.renderer.layer;

import java.util.Collection;
import java.util.List;

public record BlendItem(double opacity, Layer sublayer) implements Item {
    @Override
    public Collection<? extends Layer> referredSublayers() {
        return List.of(sublayer);
    }

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.blend(opacity, sublayer);
    }
}
