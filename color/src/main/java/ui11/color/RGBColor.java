package ui11.color;

import java.nio.ByteOrder;
import java.util.Objects;

/**
 * The color components are in range [0; 1]
 */
public record RGBColor(double red, double green, double blue, double alpha,
                       ColorSpace<RGBColor> colorSpace) implements Color {

    public RGBColor {
        // JMH-val meg kéne nézni hogy ez gyorsabb-e, mint ha simán azt néznénk csak hogy >= 0 és <= 1.
        // POSITIVE_INFINITY ellenőrzés miatt valszeg már valszeg lassabb ez a bonyolítás.
        if (!(red * red <= red) || !(green * green <= green) || !(blue * blue <= blue) ||
                !(alpha * alpha <= alpha) ||
                red == Double.POSITIVE_INFINITY || green == Double.POSITIVE_INFINITY ||
                blue == Double.POSITIVE_INFINITY || alpha == Double.POSITIVE_INFINITY)
            throw new IllegalArgumentException("color components out of range [0, 1]: " +
                    red + ", " + green + ", " + blue + ", " + alpha);

        Objects.requireNonNull(colorSpace);
    }


    public RGBColor(double r, double g, double b, ColorSpace<RGBColor> colorSpace) {
        this(r, g, b, 1, colorSpace);
    }

    // multiply(double) nem jó, mert nem világos, hogy alpha-ra is vonatkozik-e
    public RGBColor multiply(double redMultiplier, double greenMultiplier, double blueMultiplier,
                             double alphaMultiplier) {
        return new RGBColor(red * redMultiplier, green * greenMultiplier, blue * blueMultiplier,
                alpha * alphaMultiplier, colorSpace);
    }

    public int redAsInt8() {
        return (int) Math.round(red * 255);
    }

    public int greenAsInt8() {
        return (int) Math.round(green * 255);
    }

    public int blueAsInt8() {
        return (int) Math.round(blue * 255);
    }

    public int alphaAsInt8() {
        return (int) Math.round(alpha * 255);
    }

    @Override
    public RGBColor withAlpha(double a) {
        return new RGBColor(red, green, blue, a, colorSpace);
    }

    public int toRGBA(ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) // common case
            return toABGR();
        Objects.requireNonNull(byteOrder);
        return toRGBA();
    }

    public int toRGBA() {
        return redAsInt8() << 24 | greenAsInt8() << 16 | blueAsInt8() << 8 | alphaAsInt8();
    }

    public int toABGR() {
        return alphaAsInt8() << 24 | blueAsInt8() << 16 | greenAsInt8() << 8 | redAsInt8();
    }

    @Override
    public RGBColor toSRGB() {
        if (colorSpace.equals(ColorSpace.sRGB))
            return this;
        else
            throw new UnsupportedOperationException();
    }

    public RGBColor lighter() {
        return new RGBColor(1 - (1 - red) * 0.5, 1 - (1 - green) * 0.5, 1 - (1 - blue) * 0.5, alpha, colorSpace);
    }

    @Override
    public Color lerp(Color b, double t) {
        RGBColor c = b instanceof RGBColor rgbaColor ? rgbaColor : b.toSRGB();
        if (colorSpace.equals(c.colorSpace))
            return new RGBColor(red * (1 - t) + c.red * t, green * (1 - t) + c.green * t,
                    blue * (1 - t) + c.blue * t, alpha * (1 - t) + c.alpha * t,
                    colorSpace);
        else
            return toSRGB().lerp(c.toSRGB(), t);
    }

    @Override
    public String toString() {
        if (!colorSpace.equals(ColorSpace.sRGB))
            throw new RuntimeException("TODO"); // ld. Color::toString javadoc

        /*
        final String s = String.format("#%02X%02X%02X", (int) (red * 255 + .5), (int) (green * 255 + .5), (int) (blue * 255 + .5));
        System.out.println("fill: " + s);
        return s;
         */

        String s = "#" + toTwoLetterHex(red) + toTwoLetterHex(green) + toTwoLetterHex(blue);
        if (alpha < 1)
            s += toTwoLetterHex(alpha);
        return s;

        /*
        // JS-ben nincs String.format, én meg lusta vagyok csinálni olyat

        if (alpha < 1)
            return "rgb(" + red * 255 + ", " + green * 255 + ", " + blue * 255 + ", " + alpha + ")";
        else
            return "rgb(" + red * 255 + ", " + green * 255 + ", " + blue * 255 + ")";

         */
    }

    private String toTwoLetterHex(double d) {
        int i = (int) Math.round(d * 255);
        if (i < 16)
            return "0" + Integer.toHexString(i);
        return Integer.toHexString(i);
    }

    public static Object ofUnsignedByteRGBA(byte r, byte g, byte b, byte a, ColorSpace<RGBColor> colorSpace) {
        return new RGBColor((r & 0xFF) / 255.0, (g & 0xFF) / 255.0,
                (b & 0xFF) / 255.0, (a & 0xFF) / 255.0, colorSpace);
    }

    public static final ColorModel<RGBColor> COLOR_MODEL = new ColorModel<RGBColor>() {
        @Override
        public RGBColor interpolate(RGBColor a, RGBColor b, double t) {
            if (!a.colorSpace().equals(b.colorSpace())) {
                a = a.toSRGB();
                b = b.toSRGB();
            }
            return new RGBColor(a.red() * (1 - t) + b.red() * t, a.green() * (1 - t) + b.green() * t,
                    a.blue() * (1 - t) + b.blue() * t, a.alpha() * (1 - t) + b.alpha() * t,
                    a.colorSpace());
        }
    };
}
