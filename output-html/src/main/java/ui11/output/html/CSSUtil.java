package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ui11.color.Color;
import ui11.color.RGBColor;

public class CSSUtil {

    private CSSUtil() {
        throw new RuntimeException("should not reach here");
    }

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    public static void color(Color color, UTF8Writer out) {
        RGBColor srgb = color.toSRGB(); // TODO

        out.writeByte('#');
        printAsHexOctet(srgb.red(), out);
        printAsHexOctet(srgb.green(), out);
        printAsHexOctet(srgb.blue(), out);
        if (srgb.alpha() != 1)
            printAsHexOctet(srgb.alpha(), out);
    }

    private static void printAsHexOctet(double d, UTF8Writer out) {
        int i = (int) (d * 255 + .5);
        out.write(HEX[i >>> 4]);
        out.write(HEX[i & 0xF]);
    }
}
