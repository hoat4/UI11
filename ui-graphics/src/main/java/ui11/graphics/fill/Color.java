package ui11.graphics.fill;

import ui11.geom.Lerpable;

import javax.annotation.Nonnull;

/**
 * RGBA szín. 0 és 1 közötti a komponensek értékkészlete.
 */
public record Color(double red, double green, double blue, double alpha) implements Lerpable<Color> {

    public Color {
        if (red * red > red || green * green > green || blue * blue > blue || alpha * alpha > alpha)
            throw new IllegalArgumentException(red + ", " + green + ", " + blue + ", " + alpha);
    }

    public Color(double r, double g, double b) {
        this(r, g, b, 1);
    }

    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    public static final Color WHITE = new Color(1, 1, 1);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color RED = new Color(1, 0, 0);
    public static final Color GREEN = new Color(0, 1, 0);
    public static final Color BLUE = new Color(0, 0, 1);
    public static final Color YELLOW = new Color(1, 1, 0);
    public static final Color MAGENTA = new Color(1, 0, 1);
    public static final Color CYAN = new Color(0, 1, 1);
    public static final Color GRAY = new Color(0.5, 0.5, 0.5);
    public static final Color PINK = new Color(1, 175.0 / 255, 175.0 / 255);
    public static final Color ORANGE = Color.parse("#FFA500");
    public static final Color LIGHTGREEN = Color.parse("#AFA");
    public static final Color LIGHTCORAL = Color.parse("#F08080");
    public static final Color LIGHTBLUE = Color.parse("#ADD8E6");
    public static final Color LIGHTGRAY = gray(.75);

    public static Color gray(double v) {
        return new Color(v, v, v);
    }

    public static Color parse(String s) {
        return of(s);
    }

    /**
     * A megadott szövegnek kettőskereszttel kell kezdődnie, majd utána 3, 6, 4 vagy 8 hexadecimális számjegyből
     * állnia.
     */
    public static Color of(String s) {
        if (s.startsWith("rgb("))
            return parseNonHexSyntax(s);
        if (!s.startsWith("#"))
            throw parseError(s);
        return switch (s.length()) {
            case 4 -> new Color(
                    Integer.parseInt(s.substring(1, 2), 16) / 15.0,
                    Integer.parseInt(s.substring(2, 3), 16) / 15.0,
                    Integer.parseInt(s.substring(3, 4), 16) / 15.0
            );
            case 7 -> new Color(
                    Integer.parseInt(s.substring(1, 3), 16) / 255.0,
                    Integer.parseInt(s.substring(3, 5), 16) / 255.0,
                    Integer.parseInt(s.substring(5, 7), 16) / 255.0
            );
            case 5 -> new Color(
                    Integer.parseInt(s.substring(1, 2), 16) / 15.0,
                    Integer.parseInt(s.substring(2, 3), 16) / 15.0,
                    Integer.parseInt(s.substring(3, 4), 16) / 15.0,
                    Integer.parseInt(s.substring(4, 5), 16) / 15.0
            );
            case 9 -> new Color(
                    Integer.parseInt(s.substring(1, 3), 16) / 255.0,
                    Integer.parseInt(s.substring(3, 5), 16) / 255.0,
                    Integer.parseInt(s.substring(5, 7), 16) / 255.0,
                    Integer.parseInt(s.substring(7, 9), 16) / 255.0
            );
            default -> throw parseError(s);
        };
    }

    /**
     * "rgb("-vel kezdődő szímegadás parzolása
     */
    private static Color parseNonHexSyntax(String s) {
        if (!s.endsWith(")"))
            throw parseError(s);
        String[] split = s.substring(4, s.length() - 1).split(" ");
        return new Color(percentage(s, split[0]), percentage(s, split[1]), percentage(s, split[2]));
    }

    private static double percentage(String s, String s2) {
        if (!s2.endsWith("%"))
            throw parseError(s);
        int i;
        try {
            i = Integer.parseInt(s2.substring(0, s2.length() - 1));
        } catch (NumberFormatException e) {
            throw parseError(s);
        }
        if (i < 0 || i > 100)
            throw parseError(s);
        return i / 100.0;
    }

    @Nonnull
    private static IllegalArgumentException parseError(String s) {
        return new IllegalArgumentException("not a valid color string: \"" + s + "\"");
    }

    public Color lerp(Color b, double t) {
        return new Color(red * (1 - t) + b.red * t, green * (1 - t) + b.green * t,
                blue * (1 - t) + b.blue * t, alpha * (1 - t) + b.alpha * t);
    }

    /**
     * @return CSS-kompatibilis RGB szín formátum
     */
    @Override
    public String toString() {
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

    public Color withAlpha(double a) {
        return new Color(red, green, blue, a);
    }

    // https://gist.github.com/Yona-Appletree/0c4b58763f070ae8cdff7db583c82563
    public static Color hsl(double hue, double saturation, double ligthness) {
        double h = hue, s = saturation, l = ligthness;

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
        return new Color(r, g, b);
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

    public Color lighter() {
        return new Color(1 - (1 - red) * 0.5, 1 - (1 - green) * 0.5, 1 - (1 - blue) * 0.5, alpha);
    }
}
