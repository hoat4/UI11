package ui11.renderer.j2d;

import ui11.geom.Location;
import ui11.observable.MutableObservable;
import ui11.renderer.FramebufferSize;
import ui11.renderer.layer.Layer;

import java.awt.*;

public class J2DLayer extends Layer {

    private final Location.CoordinateSpace coordinateSpace;
    FramebufferSize bufferSize;
    final MutableObservable<Image> content = MutableObservable.ofNullable();

    public J2DLayer(Location.CoordinateSpace coordinateSpace) {
        this.coordinateSpace = coordinateSpace;
    }

    @Override
    public LayerUpdater createLayerUpdater() {
        return new J2DLayerUpdater(this);
    }

    public Location.CoordinateSpace coordinateSpace() {
        return coordinateSpace;
    }
}
