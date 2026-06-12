package ui11.geom;

import org.jspecify.annotations.Nullable;

/**
 * az mezőnevekben első számjegy az oszlop, a második számjegy a sor
 *
 * @param m00 vízszintes skálázás (a mátrix 1. sorának 1. oszlopa)
 * @param m10 vízszintes nyírás (a mátrix 1. sorának 2. oszlopa)
 * @param m30 vízszintes eltolás (a mátrix 1. sorának 4. oszlopa)
 * @param m01 függőleges nyírás (a mátrix 2. sorának 1. oszlopa)
 * @param m11 függőleges skálázás (a mátrix 2. sorának 2. oszlopa)
 * @param m31 függőleges eltolás (a mátrix 2. sorának 4. oszlopa)
 **/
public record Mat4(double m00, double m10, double m20, double m30,
                   double m01, double m11, double m21, double m31,
                   double m02, double m12, double m22, double m32,
                   double m03, double m13, double m23, double m33)
        implements Lerpable<Mat4> {

    public static final Mat4 IDENTITY = new Mat4(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    );

    public Mat4(double d) {
        this(
                d, 0, 0, 0,
                0, d, 0, 0,
                0, 0, d, 0,
                0, 0, 0, d
        );
    }


    public static Mat4 ofColumns(Vec4 x, Vec4 y, Vec4 z, Vec4 w) {
        return new Mat4(
                x.x(), y.x(), z.x(), w.x(),
                x.y(), y.y(), z.y(), w.y(),
                x.z(), y.z(), z.z(), w.z(),
                x.w(), y.w(), z.w(), w.w()
        );
    }

    public static Mat4 ofRows(Vec4 x, Vec4 y, Vec4 z, Vec4 w) {
        return new Mat4(
                x.x(), x.y(), x.z(), x.w(),
                y.x(), y.y(), y.z(), y.w(),
                z.x(), z.y(), z.z(), z.w(),
                w.x(), w.y(), w.z(), w.w()
        );
    }

    public static Mat4 ofDiagonal(double x, double y, double z, double w) {
        return new Mat4(
                x, 0, 0, 0,
                0, y, 0, 0,
                0, 0, z, 0,
                0, 0, 0, w
        );
    }

    /**
     * Creates matrix to be a symmetric perspective projection frustum transformation for a right-handed coordinate
     * system using the given NDC z range.
     *
     * @param fovy       the vertical field of view in radians (must be greater than zero and less than
     *                   {@link Math#PI PI})
     * @param aspect     the aspect ratio (i.e. width / height; must be greater than zero)
     * @param zNear      near clipping plane distance. If the special value {@link Double#POSITIVE_INFINITY} is used,
     *                   the near clipping plane will be at positive infinity. In that case, <code>zFar</code> may not
     *                   also be {@link Double#POSITIVE_INFINITY}.
     * @param zFar       far clipping plane distance. If the special value {@link Double#POSITIVE_INFINITY} is used, the
     *                   far clipping plane will be at positive infinity. In that case, <code>zNear</code> may not also
     *                   be {@link Double#POSITIVE_INFINITY}.
     * @param zZeroToOne whether to use Vulkan's and Direct3D's NDC z range of <code>[0..+1]</code> when
     *                   <code>true</code> or whether to use OpenGL's NDC z range of <code>[-1..+1]</code> when
     *                   <code>false</code>
     */
    public static Mat4 perspective(double fovy, double aspect, double zNear, double zFar, boolean zZeroToOne) {
        // org.joml.Matrix4d::setPerspective-ből másolva

        double h = Math.tan(fovy * 0.5);
        boolean farInf = zFar > 0 && Double.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Double.isInfinite(zNear);
        double m22, m32;
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            double e = 1E-6;
            m22 = e - 1.0;
            m32 = (e - (zZeroToOne ? 1.0 : 2.0)) * zNear;
        } else if (nearInf) {
            double e = 1E-6;
            m22 = (zZeroToOne ? 0.0 : 1.0) - e;
            m32 = ((zZeroToOne ? 1.0 : 2.0) - e) * zFar;
        } else {
            m22 = (zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar);
            m32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        return new Mat4(
                1.0 / (h * aspect), 0.0, 0.0, 0.0,
                0.0, 1.0 / h, 0.0, 0.0,
                0.0, 0.0, m22, m32,
                0, 0, -1, 0
        );
    }


    public static Mat4 perspective(double fovy, double aspect, double near, double far) {
        double tanHalfFovy = Math.tan(0.5 * fovy);
        return new Mat4(
                1. / (aspect * tanHalfFovy), 0, 0, 0,
                0, 1. / (tanHalfFovy), 0, 0,
                0, 0, -(far + near) / (far - near), -2. * far * near / (far - near),
                0, 0, -1, 0
        );
    }


    public static Mat4 ortho(double left, double right, double bottom, double top, double near, double far) {
        return new Mat4(
                2. / (right - left), 0, 0, -(right + left) / (right - left),
                0, 2. / (top - bottom), 0, -(top + bottom) / (top - bottom),
                0, 0, -2. / (far - near), -(far + near) / (far - near),
                0, 0, 0, 1
        );
    }

    public static Mat4 lookAt(Vec3 eye, Vec3 center, Vec3 up) {
        double eyeX = eye.x(), eyeY = eye.y(), eyeZ = eye.z();
        double centerX = center.x(), centerY = center.y(), centerZ = center.z();
        double upX = up.x(), upY = up.y(), upZ = up.z();

        // Compute direction from position to lookAt
        double dirX, dirY, dirZ;
        dirX = eyeX - centerX;
        dirY = eyeY - centerY;
        dirZ = eyeZ - centerZ;
        // Normalize direction
        double invDirLength = 1.0 / Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        dirX *= invDirLength;
        dirY *= invDirLength;
        dirZ *= invDirLength;
        // left = up x direction
        double leftX, leftY, leftZ;
        leftX = upY * dirZ - upZ * dirY;
        leftY = upZ * dirX - upX * dirZ;
        leftZ = upX * dirY - upY * dirX;
        // normalize left
        double invLeftLength = 1.0 / Math.sqrt(leftX * leftX + leftY * leftY + leftZ * leftZ);
        leftX *= invLeftLength;
        leftY *= invLeftLength;
        leftZ *= invLeftLength;
        // up = direction x left
        double upnX = dirY * leftZ - dirZ * leftY;
        double upnY = dirZ * leftX - dirX * leftZ;
        double upnZ = dirX * leftY - dirY * leftX;

        double m30 = -(leftX * eyeX + leftY * eyeY + leftZ * eyeZ);
        double m31 = -(upnX * eyeX + upnY * eyeY + upnZ * eyeZ);
        double m32 = -(dirX * eyeX + dirY * eyeY + dirZ * eyeZ);
        return new Mat4(
                leftX, leftY, leftZ, m30,
                upnX, upnY, upnZ, m31,
                dirX, dirY, dirZ, m32,
                0, 0, 0, 1
        );
    }


    public static Mat4 from(Vec3 position, Quat quat, Vec3 scale) {
        // először rotationt előállítjuk, mert így lehetett kényelmesen kimásolni JOML-ből
        double w2 = quat.w() * quat.w();
        double x2 = quat.x() * quat.x();
        double y2 = quat.y() * quat.y();
        double z2 = quat.z() * quat.z();
        double zw = quat.z() * quat.w(), dzw = zw + zw;
        double xy = quat.x() * quat.y(), dxy = xy + xy;
        double xz = quat.x() * quat.z(), dxz = xz + xz;
        double yw = quat.y() * quat.w(), dyw = yw + yw;
        double yz = quat.y() * quat.z(), dyz = yz + yz;
        double xw = quat.x() * quat.w(), dxw = xw + xw;
        double m00 = w2 + x2 - z2 - y2;
        double m01 = dxy + dzw;
        double m02 = dxz - dyw;
        double m10 = -dzw + dxy;
        double m11 = y2 - z2 + w2 - x2;
        double m12 = dyz + dxw;
        double m20 = dyw + dxz;
        double m21 = dyz - dxw;
        double m22 = z2 - y2 - x2 + w2;

        return new Mat4(
                m00 * scale.x(), m10 * scale.y(), m20 * scale.z(), position.x(),
                m01 * scale.x(), m11 * scale.y(), m21 * scale.z(), position.y(),
                m02 * scale.x(), m12 * scale.y(), m22 * scale.z(), position.z(),
                0, 0, 0, 1
        );
    }


    public static Mat4 from(Mat3 m) {
        return new Mat4(
                m.m00(), m.m10(), m.m20(), 0,
                m.m01(), m.m11(), m.m21(), 0,
                m.m02(), m.m12(), m.m22(), 0,
                0, 0, 0, 1
        );
    }


    public static Mat4 from(Vec3 position, Quat quat) {
        // először rotationt előállítjuk, mert így lehetett kényelmesen kimásolni JOML-ből
        double w2 = quat.w() * quat.w();
        double x2 = quat.x() * quat.x();
        double y2 = quat.y() * quat.y();
        double z2 = quat.z() * quat.z();
        double zw = quat.z() * quat.w(), dzw = zw + zw;
        double xy = quat.x() * quat.y(), dxy = xy + xy;
        double xz = quat.x() * quat.z(), dxz = xz + xz;
        double yw = quat.y() * quat.w(), dyw = yw + yw;
        double yz = quat.y() * quat.z(), dyz = yz + yz;
        double xw = quat.x() * quat.w(), dxw = xw + xw;
        double m00 = w2 + x2 - z2 - y2;
        double m01 = dxy + dzw;
        double m02 = dxz - dyw;
        double m10 = -dzw + dxy;
        double m11 = y2 - z2 + w2 - x2;
        double m12 = dyz + dxw;
        double m20 = dyw + dxz;
        double m21 = dyz - dxw;
        double m22 = z2 - y2 - x2 + w2;

        return new Mat4(
                m00, m10, m20, position.x(),
                m01, m11, m21, position.y(),
                m02, m12, m22, position.z(),
                0, 0, 0, 1
        );
    }


    public static Mat4 ofScale(Vec3 scale) {
        return new Mat4(
                scale.x(), 0, 0, 0,
                0, scale.y(), 0, 0,
                0, 0, scale.z(), 0,
                0, 0, 0, 1
        );
    }

    public static Mat4 ofTranslation(Vec2 position) {
        return new Mat4(
                1, 0, 0, position.x(),
                0, 1, 0, position.y(),
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    public static Mat4 ofTranslation(Vec3 position) {
        return new Mat4(
                1, 0, 0, position.x(),
                0, 1, 0, position.y(),
                0, 0, 1, position.z(),
                0, 0, 0, 1
        );
    }

    /**
     * @param angleRad coutner clockwise
     */
    public static Mat4 rotation2D(double angleRad) {
        return new Mat4(
                Math.cos(angleRad), -Math.sin(angleRad), 0, 0,
                Math.sin(angleRad), Math.cos(angleRad), 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }


    /**
     * counterclockwise
     */
    public static Mat4 rotation2D(Vec2 pivot, double radian) {
        Mat4 rotationAndBack = new Mat4(
                Math.cos(radian), Math.sin(radian), 0, pivot.x(),
                -Math.sin(radian), Math.cos(radian), 0, pivot.y(),
                0, 0, 1, 0,
                0, 0, 0, 1
        );
        return ofTranslation(pivot.negate()).then(rotationAndBack);
    }


    /**
     * jobbra forgat fokban megadva
     */
    public static Mat4 rotation2DDegreesClockwise(Vec2 pivot, double degree) {
        return rotation2D(pivot, Math.toRadians(-degree));
    }


    /**
     * balra forgat fokban megadva
     */
    public static Mat4 rotation2DLeftDegreesCounterClockwise(Vec2 pivot, double degree) {
        return rotation2D(pivot, Math.toRadians(degree));
    }


    public static Mat4 scale2D(double sx, double sy) {
        return new Mat4(
                sx, 0, 0, 1,
                0, sy, 0, 1,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }


    public static Mat4 of2x3(double m00, double m10, double m30, double m01, double m11, double m31) {
        return new Mat4(
                m00, m10, 0, m30,
                m01, m11, 0, m31,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    public Mat4 transpose() {
        return new Mat4(
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
    }

    /**
     * @return ebben infinity-k lesznek, ha nem invertálható
     */
    public Mat4 inverse() {
        double a = m00 * m11 - m01 * m10;
        double b = m00 * m12 - m02 * m10;
        double c = m00 * m13 - m03 * m10;
        double d = m01 * m12 - m02 * m11;
        double e = m01 * m13 - m03 * m11;
        double f = m02 * m13 - m03 * m12;
        double g = m20 * m31 - m21 * m30;
        double h = m20 * m32 - m22 * m30;
        double i = m20 * m33 - m23 * m30;
        double j = m21 * m32 - m22 * m31;
        double k = m21 * m33 - m23 * m31;
        double l = m22 * m33 - m23 * m32;
        double det = a * l - b * k + c * j + d * i - e * h + f * g;
        // JOML valamiért így csinálta, nem osztással
        det = 1.0 / det;

        return new Mat4(
                (m11 * l - m12 * k + m13 * j) * det,
                (-m10 * l + m12 * i - m13 * h) * det,
                (m10 * k - m11 * i + m13 * g) * det,
                (-m10 * j + m11 * h - m12 * g) * det,
                (-m01 * l + m02 * k - m03 * j) * det,
                (m00 * l - m02 * i + m03 * h) * det,
                (-m00 * k + m01 * i - m03 * g) * det,
                (m00 * j - m01 * h + m02 * g) * det,
                (m31 * f - m32 * e + m33 * d) * det,
                (-m30 * f + m32 * c - m33 * b) * det,
                (m30 * e - m31 * c + m33 * a) * det,
                (-m30 * d + m31 * b - m32 * a) * det,
                (-m21 * f + m22 * e - m23 * d) * det,
                (m20 * f - m22 * c + m23 * b) * det,
                (-m20 * e + m21 * c - m23 * a) * det,
                (m20 * d - m21 * b + m22 * a) * det
        );
    }

    @Nullable
    public Mat4 inverseOrNull() {
        double a = m00 * m11 - m01 * m10;
        double b = m00 * m12 - m02 * m10;
        double c = m00 * m13 - m03 * m10;
        double d = m01 * m12 - m02 * m11;
        double e = m01 * m13 - m03 * m11;
        double f = m02 * m13 - m03 * m12;
        double g = m20 * m31 - m21 * m30;
        double h = m20 * m32 - m22 * m30;
        double i = m20 * m33 - m23 * m30;
        double j = m21 * m32 - m22 * m31;
        double k = m21 * m33 - m23 * m31;
        double l = m22 * m33 - m23 * m32;
        double det = a * l - b * k + c * j + d * i - e * h + f * g;

        if (Math.abs(det) <= Double.MIN_VALUE)
            return null;

        // JOML valamiért így csinálta, nem osztással
        det = 1.0 / det;

        return new Mat4(
                (m11 * l - m12 * k + m13 * j) * det,
                (-m10 * l + m12 * i - m13 * h) * det,
                (m10 * k - m11 * i + m13 * g) * det,
                (-m10 * j + m11 * h - m12 * g) * det,
                (-m01 * l + m02 * k - m03 * j) * det,
                (m00 * l - m02 * i + m03 * h) * det,
                (-m00 * k + m01 * i - m03 * g) * det,
                (m00 * j - m01 * h + m02 * g) * det,
                (m31 * f - m32 * e + m33 * d) * det,
                (-m30 * f + m32 * c - m33 * b) * det,
                (m30 * e - m31 * c + m33 * a) * det,
                (-m30 * d + m31 * b - m32 * a) * det,
                (-m21 * f + m22 * e - m23 * d) * det,
                (m20 * f - m22 * c + m23 * b) * det,
                (-m20 * e + m21 * c - m23 * a) * det,
                (m20 * d - m21 * b + m22 * a) * det
        );
    }

    public Mat4 inverseOrThrow() {
        double a = m00 * m11 - m01 * m10;
        double b = m00 * m12 - m02 * m10;
        double c = m00 * m13 - m03 * m10;
        double d = m01 * m12 - m02 * m11;
        double e = m01 * m13 - m03 * m11;
        double f = m02 * m13 - m03 * m12;
        double g = m20 * m31 - m21 * m30;
        double h = m20 * m32 - m22 * m30;
        double i = m20 * m33 - m23 * m30;
        double j = m21 * m32 - m22 * m31;
        double k = m21 * m33 - m23 * m31;
        double l = m22 * m33 - m23 * m32;
        double det = a * l - b * k + c * j + d * i - e * h + f * g;

        if (det == 0) // TODO mennyitől számít 0-nak a determináns?
            throw new NonInvertibleMatrixException();

        det = 1.0 / det;

        return new Mat4(
                (m11 * l - m12 * k + m13 * j) * det,
                (-m10 * l + m12 * i - m13 * h) * det,
                (m10 * k - m11 * i + m13 * g) * det,
                (-m10 * j + m11 * h - m12 * g) * det,
                (-m01 * l + m02 * k - m03 * j) * det,
                (m00 * l - m02 * i + m03 * h) * det,
                (-m00 * k + m01 * i - m03 * g) * det,
                (m00 * j - m01 * h + m02 * g) * det,
                (m31 * f - m32 * e + m33 * d) * det,
                (-m30 * f + m32 * c - m33 * b) * det,
                (m30 * e - m31 * c + m33 * a) * det,
                (-m30 * d + m31 * b - m32 * a) * det,
                (-m21 * f + m22 * e - m23 * d) * det,
                (m20 * f - m22 * c + m23 * b) * det,
                (-m20 * e + m21 * c - m23 * a) * det,
                (m20 * d - m21 * b + m22 * a) * det
        );
    }

    public double determinant() {
        double a = m00 * m11 - m01 * m10;
        double b = m00 * m12 - m02 * m10;
        double c = m00 * m13 - m03 * m10;
        double d = m01 * m12 - m02 * m11;
        double e = m01 * m13 - m03 * m11;
        double f = m02 * m13 - m03 * m12;
        double g = m20 * m31 - m21 * m30;
        double h = m20 * m32 - m22 * m30;
        double i = m20 * m33 - m23 * m30;
        double j = m21 * m32 - m22 * m31;
        double k = m21 * m33 - m23 * m31;
        double l = m22 * m33 - m23 * m32;
        return a * l - b * k + c * j + d * i - e * h + f * g;
    }

    public Mat4 plus(Mat4 b) {
        return new Mat4(
                m00 + b.m00, m10 + b.m10, m20 + b.m20, m30 + b.m30,
                m01 + b.m01, m11 + b.m11, m21 + b.m21, m31 + b.m31,
                m02 + b.m02, m12 + b.m12, m22 + b.m22, m32 + b.m32,
                m03 + b.m03, m13 + b.m13, m23 + b.m23, m33 + b.m33
        );
    }

    public Vec4 mul(Vec4 v) {
        return new Vec4(
                m00 * v.x() + m10 * v.y() + m20 * v.z() + m30 * v.w(),
                m01 * v.x() + m11 * v.y() + m21 * v.z() + m31 * v.w(),
                m02 * v.x() + m12 * v.y() + m22 * v.z() + m32 * v.w(),
                m03 * v.x() + m13 * v.y() + m23 * v.z() + m33 * v.w()
        );
    }


    public Vec2 transform(Vec2 v) {
        return mul(new Vec4(v.x(), v.y(), 0, 1)).to2D();
    }


    public Vec3 transformWithoutProjection(Vec3 v) {
        return new Vec3(
                m00 * v.x() + m10 * v.y() + m20 * v.z() + m30,
                m01 * v.x() + m11 * v.y() + m21 * v.z() + m31,
                m02 * v.x() + m12 * v.y() + m22 * v.z() + m32
        );
    }


    public float[] toColumnMajorFloatArray() {
        return new float[]{
                (float) m00, (float) m01, (float) m02, (float) m03,
                (float) m10, (float) m11, (float) m12, (float) m13,
                (float) m20, (float) m21, (float) m22, (float) m23,
                (float) m30, (float) m31, (float) m32, (float) m33
        };
    }


    public Mat4 mul(Mat4 right) {
        return new Mat4(
                m00 * right.m00 + m10 * right.m01 + m20 * right.m02 + m30 * right.m03,
                m00 * right.m10 + m10 * right.m11 + m20 * right.m12 + m30 * right.m13,
                m00 * right.m20 + m10 * right.m21 + m20 * right.m22 + m30 * right.m23,
                m00 * right.m30 + m10 * right.m31 + m20 * right.m32 + m30 * right.m33,

                m01 * right.m00 + m11 * right.m01 + m21 * right.m02 + m31 * right.m03,
                m01 * right.m10 + m11 * right.m11 + m21 * right.m12 + m31 * right.m13,
                m01 * right.m20 + m11 * right.m21 + m21 * right.m22 + m31 * right.m23,
                m01 * right.m30 + m11 * right.m31 + m21 * right.m32 + m31 * right.m33,

                m02 * right.m00 + m12 * right.m01 + m22 * right.m02 + m32 * right.m03,
                m02 * right.m10 + m12 * right.m11 + m22 * right.m12 + m32 * right.m13,
                m02 * right.m20 + m12 * right.m21 + m22 * right.m22 + m32 * right.m23,
                m02 * right.m30 + m12 * right.m31 + m22 * right.m32 + m32 * right.m33,

                m03 * right.m00 + m13 * right.m01 + m23 * right.m02 + m33 * right.m03,
                m03 * right.m10 + m13 * right.m11 + m23 * right.m12 + m33 * right.m13,
                m03 * right.m20 + m13 * right.m21 + m23 * right.m22 + m33 * right.m23,
                m03 * right.m30 + m13 * right.m31 + m23 * right.m32 + m33 * right.m33
        );
    }


    public Mat4 then(Mat4 other) {
        return other.mul(this);
    }


    public Mat4 mul(double d) {
        return new Mat4(
                m00 * d, m10 * d, m20 * d, m30 * d,
                m01 * d, m11 * d, m21 * d, m31 * d,
                m02 * d, m12 * d, m22 * d, m32 * d,
                m03 * d, m13 * d, m23 * d, m33 * d
        );
    }


    @Override
    public Mat4 lerp(Mat4 b, double t) {
        return new Mat4(
                LerpUtil.lerp(this.m00, b.m00, t),
                LerpUtil.lerp(this.m10, b.m10, t),
                LerpUtil.lerp(this.m20, b.m20, t),
                LerpUtil.lerp(this.m30, b.m30, t),
                LerpUtil.lerp(this.m01, b.m01, t),
                LerpUtil.lerp(this.m11, b.m11, t),
                LerpUtil.lerp(this.m21, b.m21, t),
                LerpUtil.lerp(this.m31, b.m31, t),
                LerpUtil.lerp(this.m02, b.m02, t),
                LerpUtil.lerp(this.m12, b.m12, t),
                LerpUtil.lerp(this.m22, b.m22, t),
                LerpUtil.lerp(this.m32, b.m32, t),
                LerpUtil.lerp(this.m03, b.m03, t),
                LerpUtil.lerp(this.m13, b.m13, t),
                LerpUtil.lerp(this.m23, b.m23, t),
                LerpUtil.lerp(this.m33, b.m33, t)
        );
    }


    public Vec4 row(int row) {
        return switch (row) {
            case 0 -> new Vec4(m00, m10, m20, m30);
            case 1 -> new Vec4(m01, m11, m21, m31);
            case 2 -> new Vec4(m02, m12, m22, m32);
            case 3 -> new Vec4(m03, m13, m23, m33);
            default -> throw new IllegalArgumentException(
                    "row number must be between 0 and 3: " + row + "; " +
                            "Matrix: \n" + this);
        };
    }

    public Vec4 column(int column) {
        return switch (column) {
            case 0 -> new Vec4(m00, m01, m02, m03);
            case 1 -> new Vec4(m10, m11, m12, m13);
            case 2 -> new Vec4(m20, m21, m22, m23);
            case 3 -> new Vec4(m30, m31, m32, m33);
            default -> throw new IllegalArgumentException(
                    "column number must be between 0 and 3: " + column + "; " +
                            "Matrix: \n" + this);
        };
    }


    @Override
    public String toString() {
        return "+- " + m00 + ", " + m10 + ", " + m20 + ", " + m30 + "\n" +
                "|  " + m01 + ", " + m11 + ", " + m21 + ", " + m31 + "\n" +
                "|  " + m02 + ", " + m12 + ", " + m22 + ", " + m32 + "\n" +
                "+- " + m03 + ", " + m13 + ", " + m23 + ", " + m33;
    }

    public Mat3 dropLastColumnAndRow() {
        return new Mat3(
                m00, m10, m20,
                m01, m11, m21,
                m02, m12, m22
        );
    }

    public boolean isIdentity() {
        return equals(Mat4.IDENTITY); // TODO equals helyett majdnem-equals kéne
    }

    public boolean isAtMost2DTranslation() {
        // ld. komment isIdentity-ben
        return m00 == 1 && m10 == 0 && m20 == 0 &&
                m01 == 0 && m11 == 1 && m21 == 0 &&
                m02 == 0 && m12 == 0 && m22 == 1 && m32 == 0 &&
                m03 == 0 && m13 == 0 && m23 == 0 && m33 == 1;
    }

    public static class NonInvertibleMatrixException extends RuntimeException {
    }
}
