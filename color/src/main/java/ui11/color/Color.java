package ui11.color;

import ui11.geom.Lerpable;

/**
 * RGBA szín. 0 és 1 közötti a komponensek értékkészlete.
 */
public interface Color extends Lerpable<Color> {

    RGBColor TRANSPARENT = sRGB(0, 0, 0, 0);
    RGBColor WHITE = sRGB(1, 1, 1);
    RGBColor BLACK = sRGB(0, 0, 0);
    RGBColor RED = sRGB(1, 0, 0);
    RGBColor GREEN = sRGB(0, 1, 0);
    RGBColor BLUE = sRGB(0, 0, 1);
    RGBColor YELLOW = sRGB(1, 1, 0);
    RGBColor MAGENTA = sRGB(1, 0, 1);
    RGBColor CYAN = sRGB(0, 1, 1);
    RGBColor GRAY = sRGB(0.5, 0.5, 0.5);
    RGBColor PINK = sRGB(1, 175.0 / 255, 175.0 / 255);
    RGBColor ORANGE = (RGBColor) Color.parse("#FFA500");
    RGBColor LIGHTGREEN = (RGBColor) Color.parse("#AFA");
    RGBColor LIGHTCORAL = (RGBColor) Color.parse("#F08080");
    RGBColor LIGHTBLUE = (RGBColor) Color.parse("#ADD8E6");
    RGBColor LIGHTGRAY = gray(.75);

    double alpha();

    Color withAlpha(double a);

    ColorSpace<?> colorSpace();

    RGBColor toSRGB();

    /**
     * @return CSS-kompatibilis RGB szín formátum
     */
    // TODO ne legyen megkövetelve hogy CSS formátumú legyen. de ehhez meg kéne keresni az összes toString hívást.
    @Override
    String toString();

    @Override
    default Color lerp(Color b, double t){
        if (colorSpace().colorModel().equals(b.colorSpace().colorModel()))
            return lerpHelper(colorSpace().colorModel(), b, t);
        else
            return toSRGB().lerp(b.toSRGB(), t);
    }

    @SuppressWarnings("unchecked")
    private <C extends Color> Color lerpHelper(ColorModel<C> colorModel, Color b, double t) {
        return colorModel.interpolate((C)this, (C)b, t);
    }

    public static RGBColor sRGB(double r, double g, double b) {
        return new RGBColor(r, g, b, 1, ColorSpace.sRGB);
    }
    
    public static RGBColor sRGB(double r, double g, double b, double a) {
        return new RGBColor(r, g, b, a, ColorSpace.sRGB);
    }
    
    public static RGBColor gray(double v) {
        return sRGB(v, v, v);
    }

    public static Color parse(String s) {
        return of(s);
    }

    /**
     * A megadott szövegnek kettőskereszttel kell kezdődnie, majd utána 3, 6, 4 vagy 8 hexadecimális számjegyből
     * állnia.
     */
    public static Color of(String s) {
        return ColorStringParser.parse(s);
    }
}
