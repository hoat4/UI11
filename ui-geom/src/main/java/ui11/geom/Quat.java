package ui11.geom;

public record Quat(double w, double x, double y, double z) {
    public static final Quat IDENTITY = new Quat(1, 0, 0, 0);

    
    public Quat() {
        this(1, 0, 0, 0);
    }

    
    public static Quat of(Vec3 v) {
        return new Quat(0, v.x(), v.y(), v.z());
    }

    
    public Quat plus(Quat b) {
        return new Quat(w + b.w, x + b.x, y + b.y, z + b.z);
    }

    
    public Quat minus(Quat b) {
        return new Quat(w - b.w, x - b.x, y - b.y, z - b.z);
    }

    
    public Quat mul(double d) {
        return new Quat(w * d, x * d, y * d, z * d);
    }

    
    public Quat mul(Quat b) {
        return new Quat(
            this.w * b.w - this.x * b.x - this.y * b.y - this.z * b.z,
            this.w * b.x + this.x * b.w + this.y * b.z - this.z * b.y,
            this.w * b.y - this.x * b.z + this.y * b.w + this.z * b.x,
            this.w * b.z + this.x * b.y - this.y * b.x + this.z * b.w
        );
    }


    public Quat negate() {
        return new Quat(-w, -x, -y, -z);
    }
    

    public double dot(Quat b) {
        return this.x * b.x + this.y * b.y + this.z * b.z + this.w * b.w;
    }

    
    public double length() {
        return Math.sqrt(this.dot(this));
    }

    
    @Override
    public String toString() {
        return (w + ", " + x + ", " + y + ", " + z);
    }

    
    public Mat4 toMatrix() {
        // org.joml.Matrix4d::rotationből másolva

        double w2 = w * w;
        double x2 = x * x;
        double y2 = y * y;
        double z2 = z * z;
        double zw = z * w, dzw = zw + zw;
        double xy = x * y, dxy = xy + xy;
        double xz = x * z, dxz = xz + xz;
        double yw = y * w, dyw = yw + yw;
        double yz = y * z, dyz = yz + yz;
        double xw = x * w, dxw = xw + xw;
        return new Mat4(
                w2 + x2 - z2 - y2, -dzw + dxy, dyw + dxz, 0,
                dxy + dzw, y2 - z2 + w2 - x2, dyz - dxw, 0,
                dxz - dyw, dyz + dxw, z2 - y2 - x2 + w2, 0,
                0, 0, 0, 1
        );
    }

    
    public Quat rotate(double degree, Vec3 axis) {
        return rotateRad(Math.toRadians(degree), axis);
    }
    

    public Quat rotateRad(double angle, Vec3 axis) {
        double axisX = axis.x(), axisY = axis.y(), axisZ = axis.z();

        var a = angle * .5;
        var s = Math.sin(a);

        return new Quat(
            Math.cos(a),
            axisX * s,
            axisY * s,
            axisZ * s
        );
    }
    
    public static Quat fromRot(double degree, Vec3 axis) {
        return Quat.IDENTITY.rotate(degree, axis);
    }

    public static Quat fromRotRad(double rad, Vec3 axis) {
        return Quat.IDENTITY.rotateRad(rad, axis);
    }
    
    public Quat normalize() {
        // copied from glm::quat::normalize

        var len = this.length();
        // @Todo: exception if length is less than 0?
        var oneOverLen = 1.0 / len;
        return new Quat(this.w * oneOverLen, this.x * oneOverLen, this.y * oneOverLen, this.z * oneOverLen);
    }

    
    public Quat addScaledVector(Vec3 v, double scale) {
        Quat tmp = new Quat(
            0,
            v.x() * scale,
            v.y() * scale,
            v.z() * scale
        );
        tmp = tmp.mul(this);
        return new Quat(
            this.w + tmp.w * 0.5,
            this.x + tmp.x * 0.5,
            this.y + tmp.y * 0.5,
            this.z + tmp.z * 0.5
        );
    }

    
    public static Quat lerp(Quat a, Quat b, double t) {
        return new Quat(
            LerpUtil.lerp(a.w, b.w, t),
            LerpUtil.lerp(a.x, b.x, t),
            LerpUtil.lerp(a.y, b.y, t),
            LerpUtil.lerp(a.z, b.z, t)
        );
    }


    /**
     * visszaadja, hogy hány fokkal (radián) forgat a quaternion az adott bázis körül
     */
    public double rotationAround(Vec3 v) {
        final var o = new Vec3(-v.y(), v.x(), 0);
        return o.rotate(this).angleBetween(o);
    }
    

    public boolean isNan() {
        return Double.isNaN(w) || Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
    }
    
    
    // from: https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/math/Quaternion.java#L367
    public static Quat lookAt(Vec3 direction, Vec3 up) {
        var d = direction.normalize();
        var r = up.cross(direction).normalize();
        var u = direction.cross(r).normalize();
        return fromAxes(r, u, d);
    }


    // from: https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/math/Quaternion.java#L367
    public static Quat fromAxes(Vec3 x, Vec3 y, Vec3 z) {
        return fromRotationMatrix(x.x(), y.x(), z.x(), x.y(), y.y(), z.y(), x.z(), y.z(), z.z());
    }


    // from: https://github.com/jMonkeyEngine/jmonkeyengine/blob/master/jme3-core/src/main/java/com/jme3/math/Quaternion.java#L367
    public static Quat fromRotationMatrix(
        double m00, double m01, double m02,
        double m10, double m11, double m12,
        double m20, double m21, double m22
    ) {
        double w, x, y, z;

        var lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / Math.sqrt(lengthSquared);
            m00 *= lengthSquared;
            m10 *= lengthSquared;
            m20 *= lengthSquared;
        }
        lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / Math.sqrt(lengthSquared);
            m01 *= lengthSquared;
            m11 *= lengthSquared;
            m21 *= lengthSquared;
        }
        lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = 1.0f / Math.sqrt(lengthSquared);
            m02 *= lengthSquared;
            m12 *= lengthSquared;
            m22 *= lengthSquared;
        }
        var t = m00 + m11 + m22;

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            var s = Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s;                 // so this division isn't bad
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            var s = Math.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            var s = Math.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            var s = Math.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }

        return new Quat(w, x, y, z);
    }
    

    public static Quat slerp(Quat a, Quat b, double t) {
        var cos_angle = a.dot(b);
        double w, x, y, z;
        
    	if (cos_angle < 0.) {
    		b = b.negate();
    		cos_angle = -cos_angle;
    	}
    	
    	if (cos_angle > 1 - 1e-15) {
    		x = a.x + (b.x-a.x)*t;
    		y = a.y + (b.y-a.y)*t;
    		z = a.z + (b.z-a.z)*t;
    		w = a.w + (b.w-a.w)*t;
    		return new Quat(w, x, y, z);
    	}

    	var angle     = Math.acos(cos_angle);
    	var sin_angle = Math.sin(angle);
    	var factor_a  = Math.sin((1-t) * angle) / sin_angle;
    	var factor_b  = Math.sin(t * angle)     / sin_angle;

    	x = factor_a * a.x + factor_b * b.x;
    	y = factor_a * a.y + factor_b * b.y;
    	z = factor_a * a.z + factor_b * b.z;
    	w = factor_a * a.w + factor_b * b.w;
    	
		return new Quat(w, x, y, z);
    }
}
