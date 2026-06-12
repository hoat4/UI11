package ui11.designtoken.model.parser;

import ui11.designtoken.model.ValueOrRef;
import ui11.designtoken.model.parser.JSONParser.JSONSyntaxException;
import ui11.designtoken.model.tokenvalue.*;
import ui11.designtoken.model.tokenvalue.ColorTokenValue.ColorSpace;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class DesignTokensParser {

    private final JSONParser jsonParser;

    public DesignTokensParser(JSONParser jsonParser) {
        this.jsonParser = jsonParser;
    }

    private <V extends TokenValue> ValueOrRef<V> parseValueOrRef(Class<V> tokenValueType) {
        throw new RuntimeException("TODO");
    }

    private enum TokenValueType {
        BORDER, COLOR, CUBIC_BEZIER,
        DIMENSION, DURATION, FONT_FAMILY,
        FONT_WEIGHT, GRADIENT, NUMBER,
        SHADOW, STROKE_STYLE, TRANSITION,
        TYPOGRAPHY
    }

    private class TokenValueFields {

        final Set<TokenValueField> setFields = EnumSet.noneOf(TokenValueField.class);

        // Border
        ValueOrRef<ColorTokenValue> color;
        ValueOrRef<DimensionTokenValue> width;
        ValueOrRef<StrokeStyleTokenValue> style;

        // Color
        ColorSpace colorSpace;
        List<ValueOrRef<NumberTokenValue>> components;
        Double alpha;
        String fallbackHexColorNotation;

        void parse(JSONParser.ValueType valueType) throws JSONSyntaxException, IOException {
            switch (valueType) {
                case OBJECT -> {
                    for (String propName; (propName = jsonParser.nextPropertyName()) != null;) {
                        parseProperty(propName);
                    }
                }
            }
        }

        private void parseProperty(String name) {
            // designtokens spec nem írja hogy mi a teendő duplicate property esetén
            // RFC 8259 csak annyit ír, hogy should be unique.

            switch (name) {
                case "color" -> {
                    color = parseValueOrRef(ColorTokenValue.class);
                    setFields.add(TokenValueField.COLOR);
                }
                case "width" -> {
                    width = parseValueOrRef(DimensionTokenValue.class);
                    setFields.add(TokenValueField.WIDTH);
                }
                case "style" -> {
                    style = parseValueOrRef(StrokeStyleTokenValue.class);
                    setFields.add(TokenValueField.STYLE);
                }

            }
        }
    }

    private enum TokenValueField {
        COLOR, // Border, GradientStop, Shadow
        WIDTH, STYLE, // Border
        COLOR_SPACE, COMPONENTS, ALPHA, FALLBACK_HEX_COLOR, // Color,
        P1X, P1Y, P2X, P2Y, // CubicBezier
        VALUE_PROP, UNIT, // Dimension, Duration
        INLINE_VALUE_STRING, // FontFamily
        INLINE_VALUE_NUMBER, // FontWeight, Number
        INLINE_VALUE_REF, // StrokeStyle, Shadow, Gradient, Shadow
        POSITION, // GradientStop
        OFFSET_X, OFFSET_Y, BLUR, SPREAD, INSET, // Shadow
        DURATION, DELAY, TIMING_FUNCTION, // Transition
        FONT_FAMILY, FONT_SIZE, FONT_WEIGHT, LETTER_SPACING, LINE_HEIGHT // Typography
    }
}
