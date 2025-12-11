package ui11.geom;

import glm_.mat3x3.Mat3d;
import glm_.mat4x4.Mat4d;
import glm_.quat.QuatD;
import ui11.geom.Mat3;
import ui11.geom.Mat4;
import ui11.geom.Quat;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestUtils {
    public static boolean doubleEquals(double a, double b) {
        final double eps = 0.00000000000001;
        return Math.abs(a - b) < eps;
    }


    public static boolean mat4Equals(Mat4 a, Mat4d b) {
        return (
            TestUtils.doubleEquals(a.m00(), b.get(0,0)) &&
            TestUtils.doubleEquals(a.m01(), b.get(0,1)) &&
            TestUtils.doubleEquals(a.m02(), b.get(0,2)) &&
            TestUtils.doubleEquals(a.m03(), b.get(0,3)) &&
            TestUtils.doubleEquals(a.m10(), b.get(1,0)) &&
            TestUtils.doubleEquals(a.m11(), b.get(1,1)) &&
            TestUtils.doubleEquals(a.m12(), b.get(1,2)) &&
            TestUtils.doubleEquals(a.m13(), b.get(1,3)) &&
            TestUtils.doubleEquals(a.m20(), b.get(2,0)) &&
            TestUtils.doubleEquals(a.m21(), b.get(2,1)) &&
            TestUtils.doubleEquals(a.m22(), b.get(2,2)) &&
            TestUtils.doubleEquals(a.m23(), b.get(2,3)) &&
            TestUtils.doubleEquals(a.m30(), b.get(3,0)) &&
            TestUtils.doubleEquals(a.m31(), b.get(3,1)) &&
            TestUtils.doubleEquals(a.m32(), b.get(3,2)) &&
            TestUtils.doubleEquals(a.m33(), b.get(3,3))
        );
    }


    public static boolean mat3Equals(Mat3 a, Mat3d b) {
        return (
            TestUtils.doubleEquals(a.m00(), b.get(0,0)) &&
            TestUtils.doubleEquals(a.m01(), b.get(0,1)) &&
            TestUtils.doubleEquals(a.m02(), b.get(0,2)) &&
            TestUtils.doubleEquals(a.m10(), b.get(1,0)) &&
            TestUtils.doubleEquals(a.m11(), b.get(1,1)) &&
            TestUtils.doubleEquals(a.m12(), b.get(1,2)) &&
            TestUtils.doubleEquals(a.m20(), b.get(2,0)) &&
            TestUtils.doubleEquals(a.m21(), b.get(2,1)) &&
            TestUtils.doubleEquals(a.m22(), b.get(2,2))
        );
    }


    public static boolean quatEquals(Quat a, QuatD b) {
        return (
            TestUtils.doubleEquals(a.x(), b.x) &&
            TestUtils.doubleEquals(a.y(), b.y) &&
            TestUtils.doubleEquals(a.z(), b.z) &&
            TestUtils.doubleEquals(a.w(), b.w)
        );
    }


    @Test
    public void mat4Equals() {
        Mat4 m1 = new Mat4(
            0,  1,  2,  3,
            4,  5,  6,  7,
            8,  9,  10, 11,
            12, 13, 14, 15
        );
        Mat4d m2 = new Mat4d(
            0,  4,  8,  12,
            1,  5,  9,  13,
            2,  6,  10, 14,
            3,  7,  11, 15
        );

        assertTrue(TestUtils.mat4Equals(m1, m2));
    }


    @Test
    public void mat3Equals() {
        Mat3 m1 = new Mat3(
                0,  1,  2,
                4,  5,  6,
                8,  9,  10
        );
        Mat3d m2 = new Mat3d(
                0,  4,  8,
                1,  5,  9,
                2,  6,  10
        );

        assertTrue(TestUtils.mat3Equals(m1, m2));
    }


    @Test
    public void testEquals() {
        double a = 0.0001;
        double b = 0.0001;

        Assert.assertTrue(doubleEquals(a, b));
    }


    public static Mat4d toMat4d(QuatD q) {
        return toMat4d(q, new Mat4d());
    }


    public static Mat4d toMat4d(QuatD q, Mat4d res) {
        double qxx = q.x * q.x;
        double qyy = q.y * q.y;
        double qzz = q.z * q.z;
        double qxz = q.x * q.z;
        double qxy = q.x * q.y;
        double qyz = q.y * q.z;
        double qwx = q.w * q.x;
        double qwy = q.w * q.y;
        double qwz = q.w * q.z;

        res.set(0, 0, 1 - 2 * (qyy + qzz));
        res.set(0, 1, 2 * (qxy + qwz));
        res.set(0, 2, 2 * (qxz - qwy));
        res.set(1, 0, 2 * (qxy - qwz));
        res.set(1, 1, 1 - 2 * (qxx + qzz));
        res.set(1, 2, 2 * (qyz + qwx));
        res.set(2, 0, 2 * (qxz + qwy));
        res.set(2, 1, 2 * (qyz - qwx));
        res.set(2, 2, 1 - 2 * (qxx + qyy));

        return res;
    }
}
