package ui11.geom;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public sealed abstract class Shape {

    public abstract boolean contains(Location point);

    public abstract Rect bounds(Location.CoordinateSpace coordinateSpace);

    public abstract Shape inverseTransform(Mat4 transformation);

    public abstract @Nullable Rect asRect(Location.CoordinateSpace coordinateSpace);

    /**
     * @throws UnsupportedOperationException if not convertible to path, i.e. {@link #degenerateShape()}
     */
    public abstract @NonNull Path asPath(Location.CoordinateSpace coordinateSpace);

    public static Shape ofRect(Rect rect, Location.CoordinateSpace coordinateSpace) {
        return new RectShape(rect, coordinateSpace);
    }

    public static Shape ofPath(Path path, Location.CoordinateSpace coordinateSpace) {
        return new PathShape(path, coordinateSpace);
    }

    public static Shape degenerateShape() {
        return InfiniteShape.INSTANCE;
    }

    private static final class RectShape extends Shape {

        public final Rect rect;
        public final Location.CoordinateSpace coordinateSpace;

        public RectShape(Rect rect, Location.CoordinateSpace coordinateSpace) {
            this.rect = rect;
            this.coordinateSpace = coordinateSpace;
        }

        @Override
        public boolean contains(Location point) {
            return rect.contains(point.in(coordinateSpace));
        }

        @Override
        public Rect bounds(Location.CoordinateSpace coordinateSpace) {
            if (coordinateSpace.equals(this.coordinateSpace))
                return rect;
            Mat4 transformation = this.coordinateSpace.transformationTo(coordinateSpace);
            if (transformation.isAtMost2DTranslation())
                return rect.translate(new Vec2(transformation.m30(), transformation.m31()));
            else
                return Path.ofRect(rect).transform(transformation).bounds();
        }

        @Override
        public Shape inverseTransform(Mat4 transformation) {
            if (transformation.isAtMost2DTranslation())
                return new RectShape(rect.translate(new Vec2(-transformation.m30(), -transformation.m31())),
                        coordinateSpace.withTransformation(transformation));
            else

                throw new RuntimeException("TODO");
        }

        @Override
        public @Nullable Rect asRect(Location.CoordinateSpace coordinateSpace) {
            if (coordinateSpace.equals(this.coordinateSpace))
                return rect;
            Mat4 transformation = this.coordinateSpace.transformationTo(coordinateSpace);
            if (transformation.isAtMost2DTranslation())
                return rect.translate(new Vec2(transformation.m30(), transformation.m31()));
            else
                return null;
        }

        @Override
        public @NonNull Path asPath(Location.CoordinateSpace coordinateSpace) {
            return Path.ofRect(rect).transform(this.coordinateSpace.transformationTo(coordinateSpace));
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

    private static final class PathShape extends Shape {

        public final Path path;
        public final Location.CoordinateSpace coordinateSpace;

        public PathShape(Path path, Location.CoordinateSpace coordinateSpace) {
            this.path = path;
            this.coordinateSpace = coordinateSpace;
        }

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
            if (coordinateSpace.equals(this.coordinateSpace))
                return path;
            else
                return path.transform(this.coordinateSpace.transformationTo(coordinateSpace));
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

    // ezt inkább DegenerateShape-nek kéne hívni, nem infinite-nek
    private static final class InfiniteShape extends Shape {

        public static final InfiniteShape INSTANCE = new InfiniteShape();

        private InfiniteShape() {
        }

        // TODO ebből CoordinateSpaceRoot-enként nem kéne eltérő?
        // TODO "shape"-et hogyan értelmezzük 3D tér esetén?

        @Override
        public boolean contains(Location point) {
            return true;
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
            return null;
        }

        @Override
        public @NonNull Path asPath(Location.CoordinateSpace coordinateSpace) {
            throw new UnsupportedOperationException("Degenerate shape");
        }
    }
}
