package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.*;
import ui11.geom.Shape;

import java.util.ArrayList;
import java.util.List;

public sealed interface Shape2D {

    static Shape2D intersection(Shape2D a, Shape2D b) {
        return switch (a) {
            case RectShape rectA -> switch (b) {
                case RectShape rectB -> new RectShape(rectA.rect.intersect(rectB.rect));
                case InfinitePlane __ -> a;
                case GenericTransformedShape __ -> {
                    throw new RuntimeException("TODO");
                }
            };
            case InfinitePlane __ -> b;
            case GenericTransformedShape __ -> {
                throw new RuntimeException("TODO");
            }
        };
    }

    boolean contains(double x, double y);

    Shape2D createStrokedShape(double thickness);

    int estimateTriangleCount();

    void toTriangles(Triangle2DConsumer consumer);

    Shape2D transform(Mat4 m);

    Rect bounds();

    @FunctionalInterface
    interface Triangle2DConsumer {

        void accept(Vec2 a, Vec2 b, Vec2 c);
    }

    record RectShape(Rect rect) implements Shape2D {

        @Override
        public int estimateTriangleCount() {
            return 2;
        }

        @Override
        public void toTriangles(Triangle2DConsumer consumer) {
            consumer.accept(rect.topLeft(), rect.bottomRight(), rect.topRight());
            consumer.accept(rect.topLeft(), rect.bottomLeft(), rect.bottomRight());
        }

        @Override
        public boolean contains(double x, double y) {
            return rect.contains(new Vec2(x, y));
        }

        @Override
        public Shape2D createStrokedShape(double thickness) {
            throw new RuntimeException("TODO");
        }

        @Override
        public Shape2D transform(Mat4 m) {
            if (m.isAtMost2DTranslation())
                return new RectShape(rect.translate(new Vec2(m.m30(), m.m31())));
            else
                return new GenericTransformedShape(this, m);
        }

        @Override
        public Rect bounds() {
            return rect;
        }
    }

    record GenericTransformedShape(Shape2D originalShape, Mat4 matrix) implements Shape2D {

        @Override
        public boolean contains(double x, double y) {
            Mat4 inverse = matrix.inverseOrNull();
            if (inverse == null)
                return false;

            // TODO ez így értelmes?
            Vec2 inverted = inverse.transform(new Vec2(x, y));
            return originalShape.contains(inverted.x(), inverted.y());
        }

        @Override
        public Shape2D createStrokedShape(double thickness) {
            throw new RuntimeException("TODO");
        }

        @Override
        public int estimateTriangleCount() {
            return originalShape.estimateTriangleCount();
        }

        @Override
        public void toTriangles(Triangle2DConsumer consumer) {
            originalShape.toTriangles((a, b, c) -> {
                consumer.accept(matrix.transform(a), matrix.transform(b), matrix.transform(c));
            });
        }

        @Override
        public Shape2D transform(Mat4 m) {
            return new GenericTransformedShape(originalShape, m.mul(matrix));
        }

        @Override
        public Rect bounds() {
            List<Vec2> points = new ArrayList<>();
            toTriangles((a, b, c)->{
                points.add(a);
                points.add(b);
                points.add(c);
            });
            return Rect.of(points.toArray(Vec2[]::new));
        }
    }

    enum InfinitePlane implements Shape2D {

        INFINITE_PLANE;

        @Override
        public int estimateTriangleCount() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void toTriangles(Triangle2DConsumer consumer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Shape2D transform(Mat4 m) {
            return this;
        }

        @Override
        public boolean contains(double x, double y) {
            return true;
        }

        @Override
        public Shape2D createStrokedShape(double thickness) {
            throw new RuntimeException("TODO");
        }

        @Override
        public Rect bounds() {
            throw new RuntimeException("TODO");
        }
    }

    static class Shape2DAsShape extends Shape {

        private final Shape2D shape2D;
        private final Location.CoordinateSpace coordinateSpace;

        public Shape2DAsShape(Shape2D shape2D, Location.CoordinateSpace coordinateSpace) {
            this.shape2D = shape2D;
            this.coordinateSpace = coordinateSpace;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Shape2DAsShape that = (Shape2DAsShape) o;
            return shape2D.equals(that.shape2D) && coordinateSpace.equals(that.coordinateSpace);
        }

        @Override
        public int hashCode() {
            int result = shape2D.hashCode();
            result = 31 * result + coordinateSpace.hashCode();
            return result;
        }

        @Override
        public boolean contains(Location point) {
            Vec2 v = point.in(coordinateSpace);
            return shape2D.contains(v.x(), v.y());
        }

        @Override
        public Rect bounds(Location.CoordinateSpace coordinateSpace) {
            Shape2D s;
            if (coordinateSpace.equals(this.coordinateSpace))
                s = shape2D;
            else
                s = shape2D.transform(this.coordinateSpace.transformationTo(coordinateSpace));
            return s.bounds();
        }

        @Override
        public Shape inverseTransform(Mat4 transformation) {
            if (transformation.isAtMost2DTranslation() && shape2D instanceof RectShape(Rect rect))
                return new Shape2DAsShape(new RectShape(rect.translate(new Vec2(-transformation.m30(), -transformation.m31()))),
                        coordinateSpace.withTransformation(transformation));
            else
                throw new RuntimeException("TODO");
        }

        @Override
        public @Nullable Rect asRect(Location.CoordinateSpace coordinateSpace) {
            if (coordinateSpace.equals(this.coordinateSpace) && shape2D instanceof RectShape(Rect rect))
                return rect;
            else
                return null;
        }

        @Override
        public @NonNull Path asPath(Location.CoordinateSpace coordinateSpace) {
            throw new RuntimeException("TODO");
        }
    }

    static Shape2D of(Shape shape, Location.CoordinateSpace coordinateSpace) {
        Rect rect = shape.asRect(coordinateSpace);
        if (rect != null)
            return new RectShape(rect);
        else
            throw new RuntimeException("TODO");
    }
}

