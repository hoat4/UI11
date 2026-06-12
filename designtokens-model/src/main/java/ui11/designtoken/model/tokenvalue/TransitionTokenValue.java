package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

public class TransitionTokenValue extends TokenValue {

    public ValueOrRef<DurationTokenValue> duration;
    public ValueOrRef<DurationTokenValue> delay;
    public ValueOrRef<CubicBezierTokenValue> timingFunction;
}
