package ui11.geom;

// ez értelmetlen osztály, csak nem tudtam a double lerpet máshova rakni, mert MathUtil a "misc" modulban maradt
public class LerpUtil {

    private LerpUtil() {
        throw new Error();
    }

    public static double lerp(double a, double b, double t) {
        return a * (1 - t) + b * t;
    }
}
