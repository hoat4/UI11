package ui11.designtoken.model.tokenvalue;

/**
 * Represents the length of time in milliseconds an animation or animation cycle takes to complete.
 */
public class DurationTokenValue extends TokenValue {

    public double value;
    public Unit unit;

    public enum Unit {

        MILLISECOND("ms"), SECOND("s");

        public final String name;

        Unit(String name) {
            this.name = name;
        }
    }
}
