package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

import java.util.List;

public class GradientTokenValue extends TokenValue {

    /**
     * If there are no stops at the very beginning or end of the gradient axis (i.e. with position 0 or 1, respectively),
     * then the color from the stop closest to each end should be extended to that end of the axis.
     */
    public List<ValueOrRef<GradientStop>> stops;

    public static class GradientStop extends ValueElement {

        /**
         * The color value at the stop's position on the gradient.
         */
        public ValueOrRef<ColorTokenValue> color;

        /**
         * The position of the stop along the gradient's axis. The number values must be in the range [0, 1], where 0
         * represents the start position of the gradient's axis and 1 the end position. If a number value outside of
         * that range is given, it MUST be considered as if it were clamped to the range [0, 1]. For example, a value of
         * 42 should be treated as if it were 1, i.e. the end position of the gradient axis. Similarly, a value of -99
         * should be treated as if it were 0, i.e. the start position of the gradient axis.
         */
        public ValueOrRef<NumberTokenValue> position;
    }
}
