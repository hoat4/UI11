package ui11.renderer.layer;

import ui11.geom.Location;
import ui11.renderer.TextRenderer;

public record TextItem(TextRenderer.TextLayout text, Location.CoordinateSpace origin) implements Item {

    @Override
    public void visit(Layer.LayerUpdater layerUpdater) {
        layerUpdater.text(text, origin);
    }
}
