package ui11.color;

import ui11.geom.LerpUtil;

public record HSLColor(double hue, double saturation, double lightness, double alpha,
                       HSLColorSpace colorSpace) implements Color {

    public HSLColor {
        if (!(Double.isFinite(hue) && saturation >= 0 && saturation <= 1 &&
                lightness >= 0 && lightness <= 1 && alpha >= 0 && alpha <= 1))
            throw new IllegalArgumentException("HSL values out of range (-∞, ∞) × [0; 1] × [0; 1] × [0; 1]: " +
                    hue + ", " + saturation + ", " + lightness + ", " + alpha);
    }

    public HSLColor(double hue, double saturation, double lightness, HSLColorSpace colorSpace) {
        this(hue, saturation, lightness, 1, colorSpace);
    }

    public static HSLColor ofRGB(RGBColor rgbColor) {
        throw new RuntimeException("TODO");
    }

    @Override
    public Color withAlpha(double a) {
        return new HSLColor(hue, saturation, lightness, a, colorSpace);
    }

    // https://gist.github.com/Yona-Appletree/0c4b58763f070ae8cdff7db583c82563
    @Override
    public RGBColor toSRGB() {
        double h = hue, s = saturation, l = lightness;

        h = h % 360.0f;
        h /= 360f;
        s /= 100f;
        l /= 100f;

        double q = 0;

        if (l < 0.5)
            q = l * (1 + s);
        else
            q = (l + s) - (s * l);

        double p = 2 * l - q;

        double r = Math.max(0, hueToRGB(p, q, h + (1.0f / 3.0f)));
        double g = Math.max(0, hueToRGB(p, q, h));
        double b = Math.max(0, hueToRGB(p, q, h - (1.0f / 3.0f)));

        r = Math.min(r, 1.0f);
        g = Math.min(g, 1.0f);
        b = Math.min(b, 1.0f);
        return Color.sRGB(r, g, b);
    }

    private static double hueToRGB(double p, double q, double h) {
        if (h < 0) h += 1;

        if (h > 1) h -= 1;

        if (6 * h < 1) {
            return p + ((q - p) * 6 * h);
        }

        if (2 * h < 1) {
            return q;
        }

        if (3 * h < 2) {
            return p + ((q - p) * 6 * ((2.0f / 3.0f) - h));
        }

        return p;
    }


    public static final ColorModel<HSLColor> COLOR_MODEL = new ColorModel<HSLColor>() {
        @Override
        public HSLColor interpolate(HSLColor a, HSLColor b, double t) {
            if (!a.colorSpace().equals(b.colorSpace())) {
                a = HSLColor.ofRGB(a.toSRGB());
                b = HSLColor.ofRGB(b.toSRGB());
            }
            return new HSLColor(
                    LerpUtil.lerp(a.hue(), b.hue(), t),
                    LerpUtil.lerp(a.saturation(), b.saturation(), t),
                    LerpUtil.lerp(a.lightness(), b.lightness(), t),
                    LerpUtil.lerp(a.alpha(), b.alpha(), t),
                    a.colorSpace());
        }
    };
}
