package ui11.platform.opengl;

import ui11.geom.Mat4;
import ui11.geom.Rect;
import ui11.geom.Vec2;
import ui11.geom.Vec4;

import java.awt.*;

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
    }
}

