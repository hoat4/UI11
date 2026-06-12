package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

import java.util.List;

public class ShadowTokenValue extends TokenValue {

    public List<ValueOrRef<Shadow>> shadows;

    public static class Shadow extends ValueElement {

        public ValueOrRef<ColorTokenValue> color;
        public ValueOrRef<DimensionTokenValue> offsetX;
        public ValueOrRef<DimensionTokenValue> offsetY;
        public ValueOrRef<DimensionTokenValue> blur;
        public ValueOrRef<DimensionTokenValue> spread;
        public boolean inset;
    }
}
