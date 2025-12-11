package ui11.geom;

public record Vec4(double x, double y, double z, double w) implements Lerpable<Vec4> {
    public final static Vec4 UNIT = new Vec4(1);

    public Vec4(double xyzw) {
        this(xyzw, xyzw, xyzw, xyzw);
    }

    public Vec4(double xyz, double w) {
        this(xyz, xyz, xyz, w);
    }

    public Vec4(Vec3 xyz, double w) {
        this(xyz.x(), xyz.y(), xyz.z(), w);
    }

    public Vec4(Vec2 xy, Vec2 zw) {
        this(xy.x(), xy.y(), zw.x(), zw.y());
    }

    public Vec3 dropW() {
        return new Vec3(x, y, z);
    }

    public Vec4 mul(Vec4 b) {
        return new Vec4(x * b.x, y * b.y, z * b.z, w * b.w);
    }

    public Vec4 mul(double b) {
        return new Vec4(x * b, y * b, z * b, w * b);
    }

    public Vec4 plus(Vec4 b) {
        return new Vec4(x + b.x, y + b.y, z + b.z, w + b.w);
    }

    public Vec4 minus(Vec4 b) {
        return new Vec4(x - b.x, y - b.y, z - b.z, w - b.w);
    }

    public Vec4 div(double b) {
        return new Vec4(x/b, y/b, z/b, w/b);
    }

    public Vec4 withW(double w) {
        return new Vec4(x, y, z, w);
    }

    public double dot(Vec4 v) {
        return x * v.x + y * v.y + z * v.z + w * v.w;
    }

    public static Vec4 lerp(Vec4 a, Vec4 b, double t) {
        return a.mul(1 - t).plus(b.mul(t));
    }

    @Override
    public Vec4 lerp(Vec4 b, double t) {
        return lerp(this, b, t);
    }

    public float[] toArray() {
        return new float[]{(float)x, (float)y, (float)z, (float)w};
    }

    public Vec2 to2D() {
        // TODO w == 0?
        return new Vec2(x / w, y / w);
    }


    public Vec2 xy() {
        return new Vec2(x, y);
    }

    public Vec2 zw() {
        return new Vec2(z, w);
    }
}
