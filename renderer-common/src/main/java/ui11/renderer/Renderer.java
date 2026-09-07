package ui11.renderer;

import ui11.geom.Location;
import ui11.geom.Size;
import ui11.graphics.VisualContentRequest;
import ui11.observable.Observable;
import ui11.renderer.input.InputNode;

public interface Renderer<N extends Node> {

    VisualContentRequest<N> createRootContentRequest(
            Location.CoordinateSpaceRoot coordinateSpaceRoot,
            Observable<Size> size);

    void render(N root);
}
