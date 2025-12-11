package ui11.geom;

public record Mat2(
    double m00, double m10,
    double m01, double m11
) {
    
    public static final Mat2 IDENTITY = new Mat2(
        1, 0,
        0, 1
    );

    
    public static Mat2 from(double rotationRadians) {
        return new Mat2(
            Math.cos(rotationRadians), Math.sin(rotationRadians),
            -Math.sin(rotationRadians), Math.cos(rotationRadians)
        );
    }
    

    public Vec2 mul(Vec2 v) {
        return new Vec2(
                m00 * v.x() + m10 * v.y(),
                m01 * v.x() + m11 * v.y()
        );
    }

    
    public float[] toColumnMajorFloatArray() {
        return new float[]{
            (float) m00, (float) m01,
            (float) m10, (float) m11
        };
    }
}
