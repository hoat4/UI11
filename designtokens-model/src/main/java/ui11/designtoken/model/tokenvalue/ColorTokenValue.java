package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

import java.util.List;

import static ui11.designtoken.model.tokenvalue.ColorTokenValue.ColorSpace.ColorComponentRange.*;

public class ColorTokenValue extends TokenValue {

    public ColorSpace colorSpace;

    /**
     * ha none a hue, akkor NaN lesz ott
     */
    public List<ValueOrRef<NumberTokenValue>> components;

    /**
     * ha null, akkor 1-nek kell venni
     */
    public Double alpha;

    public String fallbackHexColorNotation;

    public static class ColorSpace {

        private static final ColorComponent RED = new ColorComponent("Red", ZERO_TO_ONE);
        private static final ColorComponent GREEN = new ColorComponent("Green", ZERO_TO_ONE);
        private static final ColorComponent BLUE = new ColorComponent("Blue", ZERO_TO_ONE);
        private static final ColorComponent X = new ColorComponent("X", ZERO_TO_ONE);
        private static final ColorComponent Y = new ColorComponent("Y", ZERO_TO_ONE);
        private static final ColorComponent Z = new ColorComponent("Z", ZERO_TO_ONE);
        private static final ColorComponent HUE = new ColorComponent("Hue", HUE_DEGREES);
        private static final ColorComponent SATURATION_PERCENTAGE = new ColorComponent("Saturation", PERCENTAGE);
        private static final ColorComponent LIGHTNESS_PERCENTAGE = new ColorComponent("Lightness", PERCENTAGE);
        private static final ColorComponent A = new ColorComponent("A", UNBOUNDED);
        private static final ColorComponent B = new ColorComponent("B", UNBOUNDED);
        private static final ColorComponent CHROMA = new ColorComponent("Chroma", ZERO_TO_INFINITY);
        private static final ColorComponent LIGHTNESS_UNIT_INTERVAL = new ColorComponent("Lightness", ZERO_TO_ONE);
        private static final ColorComponent WHITENESS = new ColorComponent("Whiteness", PERCENTAGE);
        private static final ColorComponent BLACKNESS = new ColorComponent("Blackness", PERCENTAGE);

        public static final ColorSpace sRGB = new ColorSpace("srgb", RED, GREEN, BLUE);
        public static final ColorSpace sRGB_LINEAR = new ColorSpace("srgb-linear", RED, GREEN, BLUE);
        public static final ColorSpace HSL = new ColorSpace("hsl", HUE, SATURATION_PERCENTAGE, LIGHTNESS_PERCENTAGE);
        public static final ColorSpace HWB = new ColorSpace("hwb", HUE, WHITENESS, BLACKNESS);
        public static final ColorSpace CIELAB = new ColorSpace("lab", LIGHTNESS_PERCENTAGE, A, B);
        public static final ColorSpace LCH = new ColorSpace("lch", LIGHTNESS_PERCENTAGE, CHROMA, HUE);
        public static final ColorSpace OKLAB = new ColorSpace("oklab", LIGHTNESS_UNIT_INTERVAL, A, B);
        public static final ColorSpace OKLCH = new ColorSpace("oklch", LIGHTNESS_UNIT_INTERVAL, CHROMA, HUE);
        public static final ColorSpace DISPLAY_P3 = new ColorSpace("display-p3", RED, GREEN, BLUE);
        public static final ColorSpace A98_RGB = new ColorSpace("a98-rgb", RED, GREEN, BLUE);
        public static final ColorSpace PROPHOTO_RGB = new ColorSpace("prophoto-rgb", RED, GREEN, BLUE);
        public static final ColorSpace REC2020 = new ColorSpace("rec2020", RED, GREEN, BLUE);
        public static final ColorSpace XYZ_D65 = new ColorSpace("xyz-d65", X, Y, Z);
        public static final ColorSpace XYZ_D50 = new ColorSpace("xyz-d50", X, Y, Z);

        private final String name;
        private final List<ColorComponent> components;

        private ColorSpace(String name, ColorComponent... components) {
            this.name = name;
            this.components = List.of(components);
        }

        public record ColorComponent(String name, ColorComponentRange range) {
        }

        public enum ColorComponentRange {
            /**
             * [0; 1]
             */
            ZERO_TO_ONE,

            /**
             * [0; 100]
             */
            PERCENTAGE,

            ZERO_TO_INFINITY,
            // TODO itt is a zártság probléma mint UNBOUNDED-nál

            /**
             * [0; 360) vagy {@code "none"}
             */
            HUE_DEGREES,

            /**
             * (-Infinity, +Infinity)
             */
            UNBOUNDED
            // TODO a specben zárt intervallumot írnak. de JSON-ban nincs szintaxis nem véges számok írására.
        }
    }
}
