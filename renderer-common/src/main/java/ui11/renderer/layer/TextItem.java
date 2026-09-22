package ui11.renderer.layer;

import ui11.color.Color;
import ui11.geom.Location;
import ui11.geom.Vec2;
import ui11.renderer.TextRenderer;

import java.util.Collection;
import java.util.List;

public record TextItem(TextRenderer.TextLayout text, Location.CoordinateSpace origin) implements Item {
    @Override
    public Collection<? extends Layer> referredSublayers() {
        return List.of();
    }

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.text(text, origin);
    }
}
