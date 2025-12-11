package ui11.geom;

import glm_.glm;
import glm_.mat3x3.Mat3d;
import glm_.mat4x4.Mat4d;
import glm_.quat.QuatD;
import glm_.vec3.Vec3d;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Mat3Tests {

    @Test
    public void skewSymmetric() {
        Mat3d mGlm = new Mat3d();
        Vec3d vGlm = new Vec3d(1, 2, 3);
        setSkewSymmetric(mGlm, vGlm);

        Vec3 v = new Vec3(1, 2, 3);
        Mat3 m = Mat3.ofSymmetricSkew(v);

        assertTrue(TestUtils.mat3Equals(m, mGlm));
    }


    @Test
    public void transformInertiaTensor() {
        Mat3d tGlm = new Mat3d(1, 2, 3);
        Mat3 t = Mat3.ofDiagonal(1, 2, 3);
        assertTrue(TestUtils.mat3Equals(t, tGlm));

        QuatD qGlm = new QuatD(1, 2, 3, 4).normalize();
        Quat q = new Quat(1, 2, 3, 4).normalize();
        assertTrue(TestUtils.quatEquals(q, qGlm));

        Mat4d rGlm = TestUtils.toMat4d(qGlm);
        Mat4 r = q.toMatrix();

        assertTrue(TestUtils.mat4Equals(r, rGlm));

        tGlm = transformInertiaTensorWithGLM(tGlm, rGlm);
        t = transformInertiaTensorWithoutGLM(t, r);
        assertTrue(TestUtils.mat3Equals(t, tGlm));
    }


    @Test
    public void determinant() {
        double d1 = new Mat3d(
            0.1, 0, 0,
            0, 0.1, 0,
            0, 0, 0.1
        ).getDet();

        double d2 = new Mat3(
            0.1, 0, 0,
            0, 0.1, 0,
            0, 0, 0.1
        ).determinant();

        assertTrue(TestUtils.doubleEquals(d1, d2));
    }


    @Test
    public void inverse() {
        Mat3d mGlm = new Mat3d(
            0.006666, 0, 0,
            0, 0.006635, 0,
            0, 0, 0.006666
        ).inverse();

        Mat3 m = new Mat3(
            0.006666, 0, 0,
            0, 0.006635, 0,
            0, 0, 0.006666
        ).inverse();

        assertTrue(TestUtils.mat3Equals(m, mGlm));
    }


    private static Mat3d transformInertiaTensorWithGLM(Mat3d inertiaTensor, Mat4d rotmat) {
        Mat3d r = new Mat3d();
        Mat3d rt = new Mat3d();
        rotmat.to(r);
        glm.INSTANCE.transpose(rt, r);

        return (inertiaTensor.times(r)).times(rt);
    }


    // jolt.InertiaTensors.transformInertiaTensor másolata
    private static Mat3 transformInertiaTensorWithoutGLM(Mat3 inertiaTensor, Mat4 rotmat) {
        Mat3 r = rotmat.dropLastColumnAndRow();
        Mat3 rt = r.transpose();

        return inertiaTensor.mul(r).mul(rt);
    }


    private static void setSkewSymmetric(Mat3d m, Vec3d v) {
        /*
        [0][1][2]
        [3][4][5]
        [6][7][8]
        */

        m.set(0, 0, 0);
        m.set(1, 1, 0);
        m.set(2, 2, 0);

        m.set(1, 0, -v.getZ());
        m.set(2, 0, +v.getY());

        m.set(0, 1, +v.getZ());
        m.set(2, 1, -v.getX());

        m.set(0, 2, -v.getY());
        m.set(1, 2, +v.getX());
    }
}
