package ui11.designtoken.model.tokenvalue;

import ui11.designtoken.model.ValueOrRef;

public class DimensionTokenValue extends TokenValue {

    // TODO nem egyértelmű, hogy a nem composite type-oknál lehet-e használni reference-eket

    public ValueOrRef<NumberTokenValue> value;
    public ValueOrRef<DimensionUnitTokenValue> unit;

    public static class DimensionUnitTokenValue extends ValueElement {

        public Unit unit;
    }

    public static enum  Unit {
        PX("px"), REM("rem");

        public final String name;

        Unit(String name) {
            this.name = name;
        }
    }
}
