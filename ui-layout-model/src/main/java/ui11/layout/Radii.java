package ui11.layout;

import ui11.geom.Length;

public record Radii(Length topLeft, Length topRight, Length bottomLeft, Length bottomRight) {

    public boolean allZero() {
        return topLeft.isZero() && topRight.isZero() && bottomLeft.isZero() && bottomRight.isZero();
    }
}
