package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

public class BorderTokenValue extends TokenValue {

    public ValueOrRef<ColorTokenValue> color;
    public ValueOrRef<DimensionTokenValue> width;
    public ValueOrRef<StrokeStyleTokenValue> style;
}
