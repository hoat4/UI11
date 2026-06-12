package ui11.geom;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record Vec3(double x, double y, double z) implements Lerpable<Vec3> {

    public static final Vec3 POSITIVE_INFINITY = new Vec3(Double.POSITIVE_INFINITY);
    public static final Vec3 NEGATIVE_INFINITY = new Vec3(Double.NEGATIVE_INFINITY);

    public static final Vec3 ZERO = new Vec3(0);
    public static final Vec3 UNIT = new Vec3(1);
    public static final Vec3 X_AXIS = new Vec3(1, 0, 0);
    public static final Vec3 Y_AXIS = new Vec3(0, 1, 0);
    public static final Vec3 Z_AXIS = new Vec3(0, 0, 1);

    public Vec3() {
        this(0);
    }

    public Vec3(double xyz) {
        this(xyz, xyz, xyz);
    }

    public Vec3(Vec2 xy, double z) {
        this(xy.x(), xy.y(), z);
    }

    public Vec3 plus(Vec3 v) {
        return new Vec3(x + v.x, y + v.y, z + v.z);
    }

    public Vec3 minus(Vec3 v) {
        return new Vec3(x - v.x, y - v.y, z - v.z);
    }

    public Vec3 mul(double b) {
        return new Vec3(x * b, y * b, z * b);
    }

    public Vec3 mul(Vec3 b) {
        return new Vec3(x * b.x, y * b.y, z * b.z);
    }

    public Vec3 div(double d) {
        return mul(1 / d);
    }

    public Vec3 cross(Vec3 v) {
        return new Vec3(
                y * v.z - z * v.y,
                z * v.x - x * v.z,
                x * v.y - y * v.x
        );
    }

    public double dot(Vec3 v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double lengthSq() {
        return x * x + y * y + z * z;
    }

    public Vec3 normalize() {
        return div(length());
    }

    public Vec3 withX(double x) {
        return new Vec3(x, y, z);
    }

    public Vec3 withY(double y) {
        return new Vec3(x, y, z);
    }

    public Vec3 withZ(double z) {
        return new Vec3(x, y, z);
    }

    public Vec3 withXZ(Vec2 xz) {
        return new Vec3(xz.x(), y, xz.y());
    }

    public Vec3 plusX(double px) {
        return new Vec3(x + px, y, z);
    }

    public Vec3 plusY(double py) {
        return new Vec3(x, y + py, z);
    }

    public Vec3 plusZ(double pz) {
        return new Vec3(x, y, z + pz);
    }

    public Vec3 minusX(double mx) {
        return new Vec3(x - mx, y, z);
    }

    public Vec3 minusY(double my) {
        return new Vec3(x, y - my, z);
    }

    public Vec3 minusZ(double mz) {
        return new Vec3(x, y, z - mz);
    }

    public static Vec3 min(Vec3 a, Vec3 b) {
        return new Vec3(
                Math.min(a.x, b.x),
                Math.min(a.y, b.y),
                Math.min(a.z, b.z)
        );
    }

    public static Vec3 min(Vec3 a, Vec3 b, Vec3 c) {
        return min(min(a, b), c);
    }

    public static Vec3 min(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
        return min(min(a, b), min(c, d));
    }

    public static Vec3 min(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 e, Vec3 f, Vec3 g, Vec3 h) {
        return min(min(a, b, c, d), min(e, f, g, h));
    }

    public static Vec3 max(Vec3 a, Vec3 b) {
        return new Vec3(
                Math.max(a.x, b.x),
                Math.max(a.y, b.y),
                Math.max(a.z, b.z)
        );
    }

    public static Vec3 max(Vec3 a, Vec3 b, Vec3 c) {
        return max(max(a, b), c);
    }

    public static Vec3 max(Vec3 a, Vec3 b, Vec3 c, Vec3 d) {
        return max(max(a, b), max(c, d));
    }

    public static Vec3 max(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 e, Vec3 f, Vec3 g, Vec3 h) {
        return max(max(a, b, c, d), max(e, f, g, h));
    }

    public static Vec3 lerp(Vec3 a, Vec3 b, double t) {
        return a.mul(1 - t).plus(b.mul(t));
    }

    @Override
    public Vec3 lerp(Vec3 b, double t) {
        return lerp(this, b, t);
    }

    public static Vec3 slerp(Vec3 a, Vec3 b, double t) {
        double dot = a.dot(b);
        dot = clamp(dot, -1.0, 1.0);
        double theta = Math.acos(dot) * t;
        Vec3 rv = b.minus(a.mul(dot)).normalize();
        return (a.mul(Math.cos(theta))).plus(rv.mul(Math.sin(theta)));
    }

    public double distanceTo(Vec3 b) {
        double dx = x - b.x, dy = y - b.y, dz = z - b.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    
    /**
     * radiánt ad vissza
     */
    public double angleBetween(Vec3 b) {
        return Math.acos(this.dot(b)/ (this.length()*b.length()));
    }

    public Vec3 with(int i, double val) {
        return switch (i) {
            case 0 -> new Vec3(val, y, z);
            case 1 -> new Vec3(x, val, z);
            case 2 -> new Vec3(x, y, val);
            default -> throw new IllegalArgumentException(
                    "specified vector component index must be " +
                            "between 0 and 2 (inclusive), but got " + i);
        };
    }

    public Vec3 applyOnEachCoordinate(LanewiseOp op) {
        return new Vec3(
                op.applyOnVectorComponent(x, 0),
                op.applyOnVectorComponent(y, 1),
                op.applyOnVectorComponent(z, 2)
        );
    }

    public Vec3 negate() {
        return mul(-1);
    }

    public Vec3 clamp(Vec3 min, Vec3 max) {
        return new Vec3(
                clamp(x, min.x(), max.x()),
                clamp(y, min.y(), max.y()),
                clamp(z, min.z(), max.z())
        );
    }

    private static double clamp(double v, double min, double max) {
        return java.lang.Math.max(min, java.lang.Math.min(max, v));
    }

    @FunctionalInterface
    public interface LanewiseOp {
        double applyOnVectorComponent(double val, int i);
    }

    public double get(int axis) {
        return get(Axis.of(axis));
    }

    public double get(Axis axis) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    public Vec3 clearAxis(int axis) {
        return with(axis, 0);
    }

    public Vec2 xz() {
        return new Vec2(x, z);
    }
    
    public Vec2 xy() {
        return new Vec2(x, y);
    }

    public Vec2 zy() {
        return new Vec2(z, y);
    }

    public Vec2 yz() {
        return new Vec2(y, z);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Vec3 o)) {
            return false;
        }

        return (
            this.x() == o.x() &&
            this.y() == o.y() &&
            this.z() == o.z()
        );
    }


    public enum Axis {
        X, Y, Z;

        private static final Axis[] AXES = values();

        public static Axis of(int index) {
            return AXES[index];
        }
    }


    @JsonIgnore // https://github.com/FasterXML/jackson-databind/issues/4157
    public boolean isNan() {
        return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }


    public Vec3 to(Vec3 to) {
        return (to.minus(this)).normalize();
    }


    public float[] toArray() {
        return new float[]{(float)x, (float)y, (float)z};
    }


    public Vec3 rotate(Quat q) {
        final var v = this;
        final var u = new Vec3(q.x(), q.y(), q.z());
        final var s = q.w();
        final var um = u.mul(2 * u.dot(v));
        final var vm = v.mul(s * s - u.dot(u));
        final var cuv = u.cross(v).mul(2 * s);
        return um.plus(vm).plus(cuv);
    }


    public Vec3 abs() {
        return new Vec3(Math.abs(x), Math.abs(y), Math.abs(z));
    }
}
