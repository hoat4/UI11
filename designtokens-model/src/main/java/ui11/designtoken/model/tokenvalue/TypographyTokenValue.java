package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

/**
 * Represents a typographic style.
 */
public class TypographyTokenValue extends TokenValue {

    /**
     * The typography's font.
     */
    public ValueOrRef<FontFamilyTokenValue> fontFamily;

    /**
     * The size of the typography.
     */
    public ValueOrRef<DimensionTokenValue> fontSize;

    /**
     * The weight of the typography.
     */
    public ValueOrRef<FontWeightTokenValue> fontWeight;

    /**
     * The horizontal spacing between characters.
     */
    public ValueOrRef<DimensionTokenValue> letterSpacing;

    /**
     * The number should be interpreted as a multiplier of the {@link #fontSize}.
     */
    public ValueOrRef<NumberTokenValue> lineHeight;
}
