package ui11.renderer.subsurface;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.*;

// Renderers should test if the shape they got is a StrokedShape, and if yes, then
// don't compute the path (or other properties) of the stroked shape, just render the stroke
// with some optimized way of the underlying platform.
public class StrokedShape extends Shape {

    public final Path path;
    public final double thickness;
    public final Location.CoordinateSpace coordinateSpace;

    public StrokedShape(Path path, double thickness, Location.CoordinateSpace coordinateSpace) {
        this.path = path;
        this.thickness = thickness;
        this.coordinateSpace = coordinateSpace;
    }

    // these methods should lazily compute the stroked shape in the future.
    // e.g. importing Marlin?

    @Override
    public boolean contains(Location point) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Rect bounds(Location.CoordinateSpace coordinateSpace) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Shape inverseTransform(Mat4 transformation) {
        throw new RuntimeException("TODO");
    }

    @Override
    public @Nullable Rect asRect(Location.CoordinateSpace coordinateSpace) {
        throw new RuntimeException("TODO");
    }

    @Override
    public @NonNull Path asPath(Location.CoordinateSpace coordinateSpace) {
        throw new RuntimeException("TODO");
    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("TODO");
    }

    @Override
    public int hashCode() {
        throw new RuntimeException("TODO");
    }
}
