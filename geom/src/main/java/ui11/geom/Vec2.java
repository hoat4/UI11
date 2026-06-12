package ui11.geom;

// TODO finite-séget nem kéne ellenőrizni?
public record Vec2(double x, double y) implements Lerpable<Vec2> {

    public static final Vec2 POSITIVE_INFINITY = new Vec2(Double.POSITIVE_INFINITY);
    public static final Vec2 NEGATIVE_INFINITY = new Vec2(Double.POSITIVE_INFINITY);

    public static final Vec2 ZERO = new Vec2(0);
    public static final Vec2 UNIT = new Vec2(1);

    public Vec2() {
        this(0, 0);
    }

    public Vec2(double xy) {
        this(xy, xy);
    }

    public Vec2 plus(Vec2 v) {
        return new Vec2(x + v.x, y + v.y);
    }

    public Vec2 plus(double dx, double dy) {
        return new Vec2(x + dx, y + dy);
    }

    public Vec2 plus(double d) {
        return new Vec2(x + d, y + d);
    }

    public Vec2 plusX(double dx) {
        return new Vec2(x + dx, y);
    }

    public Vec2 plusY(double dy) {
        return new Vec2(x, y + dy);
    }

    public Vec2 minus(Vec2 v) {
        return new Vec2(x - v.x, y - v.y);
    }

    public Vec2 minus(double dx, double dy) {
        return new Vec2(x - dx, y - dy);
    }

    public Vec2 minus(double d) {
        return new Vec2(x - d, y - d);
    }

    public Vec2 minusX(double dx) {
        return new Vec2(x - dx, y);
    }

    public Vec2 minusY(double dy) {
        return new Vec2(x, y - dy);
    }

    public Vec2 mul(double b) {
        return new Vec2(x * b, y * b);
    }

    public Vec2 mul(Vec2 b) {
        return new Vec2(x * b.x, y * b.y);
    }

    public Vec2 div(double d) {
        return mul(1 / d);
    }

    public Vec2 div(Vec2 d) {
        return mul(new Vec2(1 / d.x, 1 / d.y));
    }

    public double dot(Vec2 v) {
        return x * v.x + y * v.y;
    }

    public double length() {
        return Math.hypot(x, y);
    }

    public double lengthSq() {
        return x * x + y * y;
    }

    public Vec2 normalize() {
        return div(length());
    }

    public Vec2 withX(double x) {
        return new Vec2(x, y);
    }

    public Vec2 withY(double y) {
        return new Vec2(x, y);
    }

    public static Vec2 min(Vec2 a, Vec2 b) {
        return new Vec2(
                Math.min(a.x, b.x),
                Math.min(a.y, b.y)
        );
    }

    public static Vec2 max(Vec2 a, Vec2 b) {
        return new Vec2(
                Math.max(a.x, b.x),
                Math.max(a.y, b.y)
        );
    }

    public double distanceTo(Vec2 b) {
        double dx = x - b.x, dy = y - b.y;
        return Math.hypot(dx, dy);
    }

    public Vec2 directionTo(Vec2 dir) {
        return (dir.minus(this)).normalize();
    }

    public double angleBetween(Vec2 b) {
        return Math.acos(this.dot(b) / (this.length() * b.length()));
    }

    public Vec2 with(int i, double val) {
        return switch (i) {
            case 0 -> new Vec2(val, y);
            case 1 -> new Vec2(x, val);
            case 2 -> new Vec2(x, y);
            default -> throw new IllegalArgumentException(
                    "specified vector component index must be " +
                            "between 0 and 2 (inclusive), but got " + i);
        };
    }

    public Vec2 applyOnEachCoordinate(LanewiseOp op) {
        return new Vec2(
                op.applyOnVectorComponent(x, 0),
                op.applyOnVectorComponent(y, 1)
        );
    }

    public Vec2 negate() {
        return mul(-1);
    }

    public Vec2 clamp(Vec2 min, Vec2 max) {
        return new Vec2(
                clamp(x, min.x(), max.x()),
                clamp(y, min.y(), max.y())
        );
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public Vec2 withLength(double length) {
        return mul(length / length());
    }

    public Vec4 withZW(double z, double w) {
        return new Vec4(x, y, z, w);
    }

    @FunctionalInterface
    public interface LanewiseOp {
        double applyOnVectorComponent(double val, int i);
    }

    public static Vec2 lerp(Vec2 a, Vec2 b, double t) {
        return a.mul(1 - t).plus(b.mul(t));
    }

    @Override
    public Vec2 lerp(Vec2 b, double t) {
        return lerp(this, b, t);
    }

    public double get(int axis) {
        return get(Axis.of(axis));
    }

    public double get(Axis axis) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
        };
    }

    public Vec2 clearAxis(int axis) {
        return with(axis, 0);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Vec2 rotate90Clockwise() {
        return new Vec2(y, -x);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public Vec2 rotate90CounterClockwise() {
        return new Vec2(-y, x);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2 vec2 = (Vec2) o;
        return Double.compare(x, vec2.x) == 0 && Double.compare(y, vec2.y) == 0;
    }

    public static Vec2 ofPolarRad(double angleRad, double length) {
        return new Vec2(Math.cos(angleRad) * length, -Math.sin(angleRad) * length);
    }

    public Vec2 abs() {
        return new Vec2(
                Math.abs(x),
                Math.abs(y)
        );
    }

    public Vec2 xx() {
        return new Vec2(x, x);
    }

    public Vec2 yy() {
        return new Vec2(y, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public enum Axis {
        X, Y;

        private static final Axis[] AXES = values();

        public static Axis of(int index) {
            return AXES[index];
        }
    }
}
