package ui11.layout;

import ui11.geom.Axis;
import ui11.geom.Length;

public record Insets(Length top,
                     Length right,
                     Length bottom,
                     Length left) {

    public Insets(Length all) {
        this(all, all);
    }

    public Insets(Length topBottom, Length leftRight) {
        this(topBottom, leftRight, topBottom, leftRight);
    }

    public static Insets zero() {
        return new Insets(Length.zero(), Length.zero(), Length.zero(), Length.zero());
    }

    public static Insets all(Length all) {
        return new Insets(all);
    }

    public static Insets atTop(Length top) {
        return new Insets(top, Length.zero(), Length.zero(), Length.zero());
    }

    public static Insets atBottom(Length bottom) {
        return new Insets(Length.zero(), Length.zero(), bottom, Length.zero());
    }

    public static Insets atRight(Length right) {
        return new Insets(Length.zero(), right, Length.zero(), Length.zero());
    }

    public static Insets atLeft(Length left) {
        return new Insets(Length.zero(), Length.zero(), Length.zero(), left);
    }

    public static Insets atSide(Length leftRight) {
        return new Insets(Length.zero(), leftRight);
    }

    public static Insets atSide(Length left, Length right) {
        return new Insets(Length.zero(), right, Length.zero(), left);
    }

    public static Insets atTopBottom(Length topBottom) {
        return new Insets(topBottom, Length.zero());
    }

    public static Insets atTopBottom(Length top, Length bottom) {
        return new Insets(top, Length.zero(), bottom, Length.zero());
    }

    public Length sum(Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> left.add(right);
            case VERTICAL -> top.add(bottom);
        };
    }

    public Length begin(Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> left;
            case VERTICAL -> top;
        };
    }

    public Length end(Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> right;
            case VERTICAL -> bottom;
        };
    }

    public boolean isZero() {
        return top.isZero() && right().isZero() && bottom().isZero() && left.isZero();
    }

    public Insets multiply(double multiplier) {
        return new Insets(
                top.mul(multiplier),
                right.mul(multiplier),
                bottom.mul(multiplier),
                left.mul(multiplier)
        );
    }

    public boolean isUniform() {
        // TODO epszilon
        return top.equals(right) && top.equals(bottom) && top.equals(left);
    }

    public Insets add(Insets other) {
        return new Insets(
                top.add(other.top),
                right.add(other.right),
                bottom.add(other.bottom),
                left.add(other.left)
        );
    }

    /*
    @Creator
    public static Insets parse(String s) {
        String[] tokens = s.split("\\s+");
        return switch (tokens.length) {
            case 1 -> new Insets(Length.parse(tokens[0]));
            case 2 -> new Insets(Length.parse(tokens[0]), Length.parse(tokens[1]));
            case 3 -> new Insets(Length.parse(tokens[0]), Length.parse(tokens[1]),
                    Length.parse(tokens[2]), Length.parse(tokens[1]));
            case 4 -> new Insets(Length.parse(tokens[0]), Length.parse(tokens[1]),
                    Length.parse(tokens[2]), Length.parse(tokens[3]));
            default -> throw new RuntimeException("unknown insets: \"" + s + "\"");
        };
    }
     */
}
