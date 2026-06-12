package ui11.designtoken.model.tokenvalue;

/**
 * Represents two points (P1, P2) with one x coordinate and one y coordinate each [P1x, P1y, P2x, P2y]. The y
 * coordinates of P1 and P2 can be any real number in the range [-∞, ∞], but the x coordinates are restricted to the
 * range [0, 1].
 */
public class CubicBezierTokenValue extends TokenValue {

    public double p1x;
    public double p1y;

    public double p2x;
    public double p2y;
}
