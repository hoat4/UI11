package ui11.platform.opengl;

import ui11.geom.Location;

public class RenderingContext {

    public final Location.CoordinateSpace ndcCoordinateSpace;
    public final Shaders shaders;

    public RenderingContext(Shaders shaders, Location.CoordinateSpace ndcCoordinateSpace) {
        this.ndcCoordinateSpace = ndcCoordinateSpace;
        this.shaders = shaders;
    }
}
