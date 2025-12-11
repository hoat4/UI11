package ui11.geom;

import glm_.glm;
import glm_.mat4x4.Mat4d;
import glm_.quat.QuatD;
import glm_.vec3.Vec3d;
import ui11.geom.Mat4;
import ui11.geom.Quat;
import ui11.geom.Vec3;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class Mat4Tests {
    @Test
    public void perspective() {
        double fov    = Math.toRadians(60.);
        double aspect = 16./9.;
        double near   = 0.1;
        double far    = 100.;
        Mat4 persp = Mat4.perspective(fov, aspect, near, far);
        Mat4d perspGlm = glm.INSTANCE.perspective(fov, aspect, near, far);

        Assert.assertTrue(TestUtils.mat4Equals(persp, perspGlm));
    }


    @Test
    public void lookAt() {
        Mat4 la = Mat4.lookAt(new Vec3(0, 0, 0), new Vec3(0, 0, 1), new Vec3(0, 1, 0));
        Mat4d laGlm = glm.INSTANCE.lookAt(new Vec3d(0, 0, 0), new Vec3d(0, 0, 1), new Vec3d(0, 1, 0));

        Assert.assertTrue(TestUtils.mat4Equals(la, laGlm));
    }


    @Test
    public void calculateWorldMatrix() {
        Mat4d worldMatrixGlm = new Mat4d(1);
        Mat4d scaleMatrix = new Mat4d(1);
        Mat4d rotationMatrix = new Mat4d(1);
        Mat4d translateMatrix = new Mat4d(1);
        QuatD orientation = new QuatD();
        glm.INSTANCE.scale(scaleMatrix, 2, 5, 7, scaleMatrix);
        toMat4d(orientation, rotationMatrix);
        glm.INSTANCE.translate(translateMatrix, 12, -25, 111, translateMatrix);

        scaleMatrix.times(worldMatrixGlm, worldMatrixGlm);
        rotationMatrix.times(worldMatrixGlm, worldMatrixGlm);
        translateMatrix.times(worldMatrixGlm, worldMatrixGlm);

        Mat4 worldMatrix = Mat4.from(new Vec3(12, -25, 111), new Quat(), new Vec3(2, 5, 7));

        assertTrue(TestUtils.mat4Equals(worldMatrix, worldMatrixGlm));
    }


    private Mat4d toMat4d(QuatD q, Mat4d res) {
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
