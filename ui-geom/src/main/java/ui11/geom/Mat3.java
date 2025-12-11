package ui11.geom;

public record Mat3(double m00, double m10, double m20,
                   double m01, double m11, double m21,
                   double m02, double m12, double m22) {

    public static final Mat3 IDENTITY = new Mat3(
            1, 0, 0,
            0, 1, 0,
            0, 0, 1
    );

    public static Mat3 ofDiagonal(double d)  {
        return new Mat3(
                d, 0, 0,
                0, d, 0,
                0, 0, d
        );
    }

    public static Mat3 ofColumns(Vec3 x, Vec3 y, Vec3 z) {
        return new Mat3(
                x.x(), y.x(), z.x(),
                x.y(), y.y(), z.y(),
                x.z(), y.z(), z.z()
        );
    }

    public static Mat3 ofRows(Vec3 x, Vec3 y, Vec3 z) {
        return new Mat3(
                x.x(), x.y(), x.z(),
                y.x(), y.y(), y.z(),
                z.x(), z.y(), z.z()
        );
    }

    public static Mat3 ofDiagonal(double x, double y, double z) {
        return new Mat3(
                x, 0, 0,
                0, y, 0,
                0, 0, z
        );
    }

    public static Mat3 ofSymmetricSkew(Vec3 v) {
        return new Mat3(
                0, -v.z(), +v.y(),
                +v.z(), 0, -v.x(),
                -v.y(), +v.x(), 0
        );
    }

    public Mat3 transpose() {
        return new Mat3(
                m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22
        );
    }

    public Mat3 inverse() {
        // Changed this one to be identical to the glm implementation, because it fixed a bug where the objects would
        // rotate on the ground without stopping
        // i have no idea why that worked, im guessing it's a stability thing but dunno
        double s = 1 / determinant();
        return new Mat3(
            +(m11 * m22 - m21 * m12) * s,
            -(m10 * m22 - m20 * m12) * s,
            +(m10 * m21 - m20 * m11) * s,
            -(m01 * m22 - m21 * m02) * s,
            +(m00 * m22 - m20 * m02) * s,
            -(m00 * m21 - m20 * m01) * s,
            +(m01 * m12 - m11 * m02) * s,
            -(m00 * m12 - m10 * m02) * s,
            +(m00 * m11 - m10 * m01) * s
        );
    }

    public double determinant() {
        return (m00 * m11 - m01 * m10) * m22
                + (m02 * m10 - m00 * m12) * m21
                + (m01 * m12 - m02 * m11) * m20;
    }

    public Mat3 plus(Mat3 b) {
        return new Mat3(
                m00 + b.m00, m10 + b.m10, m20 + b.m20,
                m01 + b.m01, m11 + b.m11, m21 + b.m21,
                m02 + b.m02, m12 + b.m12, m22 + b.m22
        );
    }

    public Vec3 mul(Vec3 v) {
        return new Vec3(
                m00 * v.x() + m10 * v.y() + m20 * v.z(),
                m01 * v.x() + m11 * v.y() + m21 * v.z(),
                m02 * v.x() + m12 * v.y() + m22 * v.z()
        );
    }

    public Mat3 mul(Mat3 right) {
        return new Mat3(
                m00 * right.m00() + m10 * right.m01() + m20 * right.m02(),
                m00 * right.m10() + m10 * right.m11() + m20 * right.m12(),
                m00 * right.m20() + m10 * right.m21() + m20 * right.m22(),
                m01 * right.m00() + m11 * right.m01() + m21 * right.m02(),
                m01 * right.m10() + m11 * right.m11() + m21 * right.m12(),
                m01 * right.m20() + m11 * right.m21() + m21 * right.m22(),
                m02 * right.m00() + m12 * right.m01() + m22 * right.m02(),
                m02 * right.m10() + m12 * right.m11() + m22 * right.m12(),
                m02 * right.m20() + m12 * right.m21() + m22 * right.m22()
        );
    }

    public Mat3 mul(double d) {
        return new Mat3(
                m00 * d, m10 * d, m20 * d,
                m01 * d, m11 * d, m21 * d,
                m02 * d, m12 * d, m22 * d
        );
    }

    /*
        transform.z() gets showed to m22 (used in OverlayRenderer to send over the z value to the shader)
    */
    public static Mat3 from(Vec3 transform, Vec2 scale, double rotationRadians) {
        final var tx = transform.x();
        final var ty = transform.y();
        final var sx = scale.x();
        final var sy = scale.y();
        final var s  = Math.sin(rotationRadians);
        final var c  = Math.cos(rotationRadians);
        
        final var rm = new Mat3(
            c, s, 0,
           -s, c, 0,
            0, 0, 1
        );
        final var sm = new Mat3(
            sx, 0,  0,
            0,  sy, 0,
            0,  0,  1
        );
        final var tm = new Mat3(
            1, 0, tx,
            0, 1, ty,
            0, 0, 1
        );

        var mm = tm.mul(rm.mul(sm));
        
        return new Mat3(
            mm.m00, mm.m10, mm.m20,
            mm.m01, mm.m11, mm.m21,
            mm.m02, mm.m12, transform.z()
        );
        
    //     return new Mat3(
    //         sx * c,  sx * s, tx,
    //         sy * -s, sy * c, ty,
    //         0, 0, transform.z()
    //     );
    }

    public float[] toColumnMajorFloatArray() {
        return new float[]{
            (float) m00, (float) m01, (float) m02,
            (float) m10, (float) m11, (float) m12,
            (float) m20, (float) m21, (float) m22
        };
    }
    
    public Vec3 row(int row) {
        return switch (row) {
            case 0 -> new Vec3(m00, m10, m20);
            case 1 -> new Vec3(m01, m11, m21);
            case 2 -> new Vec3(m02, m12, m22);
            default -> throw new IllegalArgumentException(
                    "row number must be between 0 and 2: " + row + "; " +
                            "Matrix: \n" + this);
        };
    }

    public Vec3 column(int column) {
        return switch (column) {
            case 0 -> new Vec3(m00, m01, m02);
            case 1 -> new Vec3(m10, m11, m12);
            case 2 -> new Vec3(m20, m21, m22);
            default -> throw new IllegalArgumentException(
                    "column number must be between 0 and 2: " + column + "; " +
                            "Matrix: \n" + this);
        };
    }

    @Override
    public String toString() {
        return "+- " + m00 + ", " + m10 + ", " + m20 + "\n" +
                "|  " + m01 + ", " + m11 + ", " + m21 + "\n" +
                "+-  " + m02 + ", " + m12 + ", " + m22;
    }
}
