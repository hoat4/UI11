package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

import java.util.List;

public abstract sealed class StrokeStyleTokenValue extends TokenValue {

    public static final class BuiltinStrokeStyleTokenValue extends StrokeStyleTokenValue {

        public BuiltinStrokeStyle value;

        public enum BuiltinStrokeStyle {
            SOLID, DASHED, DOTTED, DOUBLE, GROOVE, RIDGE, OUTSET, INSET
        }
    }

    public static final class DashArrayStrokeStyleTokenValue extends StrokeStyleTokenValue {

        public List<ValueOrRef<DimensionTokenValue>> dashArray;

        public LineCap lineCap;

        public enum LineCap {
            ROUND, BUTT, SQUARE
        }
    }
}
