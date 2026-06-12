package ui11.color;

import org.jspecify.annotations.NonNull;

class ColorStringParser {

    private ColorStringParser() {
        throw new RuntimeException();
    }


    public static Color parse(String s) {
        if (s.startsWith("rgb("))
            return parseNonHexSyntax(s);
        if (!s.startsWith("#"))
            throw parseError(s);
        return switch (s.length()) {
            case 4 -> Color.sRGB(
                    Integer.parseInt(s.substring(1, 2), 16) / 15.0,
                    Integer.parseInt(s.substring(2, 3), 16) / 15.0,
                    Integer.parseInt(s.substring(3, 4), 16) / 15.0
            );
            case 7 -> Color.sRGB(
                    Integer.parseInt(s.substring(1, 3), 16) / 255.0,
                    Integer.parseInt(s.substring(3, 5), 16) / 255.0,
                    Integer.parseInt(s.substring(5, 7), 16) / 255.0
            );
            case 5 -> Color.sRGB(
                    Integer.parseInt(s.substring(1, 2), 16) / 15.0,
                    Integer.parseInt(s.substring(2, 3), 16) / 15.0,
                    Integer.parseInt(s.substring(3, 4), 16) / 15.0,
                    Integer.parseInt(s.substring(4, 5), 16) / 15.0
            );
            case 9 -> Color.sRGB(
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
        return Color.sRGB(percentage(s, split[0]), percentage(s, split[1]), percentage(s, split[2]));
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

    private static @NonNull IllegalArgumentException parseError(String s) {
        return new IllegalArgumentException("not a valid color string: \"" + s + "\"");
    }
}
