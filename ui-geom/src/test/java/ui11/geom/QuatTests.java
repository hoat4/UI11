package ui11.geom;

import glm_.mat4x4.Mat4d;
import glm_.quat.QuatD;
import glm_.vec3.Vec3d;
import ui11.geom.Mat4;
import ui11.geom.Quat;
import ui11.geom.Vec3;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class QuatTests {
    @Test
    public void rotate() {
        QuatD qGlm = new QuatD(1);
        qGlm.angleAxis(Math.toRadians(90), new Vec3d(0, 1, 0), qGlm);

        Quat q = new Quat();
        q = q.rotate(90, new Vec3(0, 1, 0));

        assertTrue(TestUtils.quatEquals(q, qGlm));

        qGlm.angleAxis(Math.toRadians(-45), new Vec3d(1, 0, 0), qGlm);
        q = q.rotate(-45, new Vec3(1, 0, 0));

        assertTrue(TestUtils.quatEquals(q, qGlm));
    }


    @Test
    public void toMat() {
        QuatD qGlm = new QuatD();
        qGlm.angleAxis(Math.toRadians(90), new Vec3d(0, 1, 0), qGlm);

        Quat q = new Quat();
        q = q.rotate(90, new Vec3(0, 1, 0));

        Mat4d rGlm = new Mat4d();
        TestUtils.toMat4d(qGlm, rGlm);

        Mat4 r = q.toMatrix();

        assertTrue(TestUtils.quatEquals(q, qGlm));
        assertTrue(TestUtils.mat4Equals(r, rGlm));
    }


    @Test
    public void arithmetics() {
        QuatD q1Glm = new QuatD(1, 2, 3, 4);
        QuatD q2Glm = new QuatD(4, 5, 6, 7);

        Quat q1 = new Quat(1, 2, 3, 4);
        Quat q2 = new Quat(4, 5, 6, 7);

        QuatD q3Glm = q1Glm.plus(q2Glm);
        Quat q3 = q1.plus(q2);
        assertTrue(TestUtils.quatEquals(q3, q3Glm));

        q3Glm = q1Glm.minus(q2Glm);
        q3 = q1.minus(q2);
        assertTrue(TestUtils.quatEquals(q3, q3Glm));

        q3Glm = q1Glm.times(q2Glm);
        q3 = q1.mul(q2);
        assertTrue(TestUtils.quatEquals(q3, q3Glm));
    }


    @Test
    public void normalize() {
        QuatD q1Glm = new QuatD(1, 2, 3, 4);
        Quat q1 = new Quat(1, 2, 3, 4);

        assertTrue(TestUtils.quatEquals(q1.normalize(), q1Glm.normalize()));
    }


    @Test
    public void addScaledVector() {
        QuatD qGlm = new QuatD(1, 2, 3, 4).normalize();
        Vec3d vGlm = new Vec3d(1, 2, 3);
        addScaledVector(qGlm, vGlm, 2);
        qGlm.normalizeAssign();

        Quat q = new Quat(1, 2, 3, 4).normalize();
        Vec3 v = new Vec3(1, 2, 3);
        q = q.addScaledVector(v, 2).normalize();

        assertTrue(TestUtils.quatEquals(q, qGlm));
    }


    private static void addScaledVector(QuatD q, Vec3d v, double scale) {
        QuatD tmp = new QuatD(
                0,
                v.getX() * scale,
                v.getY() * scale,
                v.getZ() * scale
        );
        tmp.times(q, tmp);
        q.set(0, q.get(0) + tmp.get(0) * 0.5);
        q.set(1, q.get(1) + tmp.get(1) * 0.5);
        q.set(2, q.get(2) + tmp.get(2) * 0.5);
        q.set(3, q.get(3) + tmp.get(3) * 0.5);
    }
}
