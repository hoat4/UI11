package ui11.geom;

import java.util.ArrayList;
import java.util.List;

public record Path(List<PathElement> items) {

    public Path {
        items = List.copyOf(items);
        if (!(items.getFirst() instanceof MoveTo))
            throw new IllegalArgumentException();
        if (items.size() < 2)
            throw new IllegalArgumentException();
    }

    public static Path ofLine(double x1, double y1, double x2, double y2) {
        return new Path(List.of(
                new MoveTo(new Vec2(x1, y1)),
                new LineTo(new Vec2(x2, y2))
        ));
    }

    /**
     * ez nem zárt görbe lesz, kivéve ha első vertex megegyezik az utolóval
     */
    public static Path ofVertices(List<Vec2> vertices) {
        PathElement[] pathElements = new PathElement[vertices.size()];
        int i = 0;
        for (Vec2 vertex : vertices)
            pathElements[i] = i++ == 0 ? new MoveTo(vertex) : new LineTo(vertex);
        return new Path(List.of(pathElements));
    }

    /**
     * zárt görbe
     */
    public static Path ofRect(Rect rect) {
        return new Path.PathBuilder().
                moveTo(rect.topLeft()).
                lineTo(rect.topRight()).
                lineTo(rect.bottomRight()).
                lineTo(rect.bottomLeft()).
                close().
                build();
    }

    public sealed interface PathElement {
    }

    public record MoveTo(Vec2 p) implements PathElement {
    }

    public record LineTo(Vec2 p) implements PathElement {
    }

    public record QuadCurveTo(Vec2 p, Vec2 control) implements PathElement {
    }

    public record CubicCurveTo(Vec2 p, Vec2 control1, Vec2 control2) implements PathElement {
    }

    public record Close() implements PathElement {
    }

    // TODO kéne ArcTo

    public static final class PathBuilder {

        private final List<PathElement> pathElements = new ArrayList<>();

        public PathBuilder moveTo(Vec2 p) {
            pathElements.add(new MoveTo(p));
            return this;
        }

        public PathBuilder moveTo(double x, double y) {
            return moveTo(new Vec2(x, y));
        }

        public PathBuilder lineTo(Vec2 p) {
            pathElements.add(new LineTo(p));
            return this;
        }

        public PathBuilder lineTo(double x, double y) {
            return lineTo(new Vec2(x, y));
        }

        public PathBuilder quadCurveTo(Vec2 p, Vec2 control) {
            pathElements.add(new QuadCurveTo(p, control));
            return this;
        }

        public PathBuilder quadCurveTo(double x, double y, double cx, double cy) {
            return quadCurveTo(new Vec2(x, y), new Vec2(cx, cy));
        }

        public PathBuilder cubicCurveTo(Vec2 p, Vec2 control1, Vec2 control2) {
            pathElements.add(new CubicCurveTo(p, control1, control2));
            return this;
        }

        public PathBuilder cubicCurveTo(double x, double y,
                                        double c1x, double c1y, double c2x, double c2y) {
            return cubicCurveTo(new Vec2(x, y), new Vec2(c1x, c1y), new Vec2(c2x, c2y));
        }

        public PathBuilder close() {
            pathElements.add(new Close());
            return this;
        }

        public Path build() {
            return new Path(pathElements);
        }
    }
}
