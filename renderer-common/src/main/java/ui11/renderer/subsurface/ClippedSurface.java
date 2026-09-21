package ui11.renderer.subsurface;

import ui11.geom.Location;
import ui11.geom.Shape;

public class ClippedSurface extends Subsurface {

    @Override
    public Shape layoutShape() {
        return parent().layoutShape();
    }

    @Override
    public Shape clipShape() {
        // TODO itt valahogy mergeölni kéne a clippeket
        throw new RuntimeException("TODO");
    }

    @Override
    public Shape inputShape() {
        throw new RuntimeException("TODO");
    }

    @Override
    public Location.CoordinateSpace coordinateSpace() {
        return parent().coordinateSpace();
    }

    @Override
    public double devicePixelRatio() {
        return 1;
    }
}