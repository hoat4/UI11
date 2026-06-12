package ui11.geom;

/**
 * Két dimenziós koordináta-rendszer tengelye (vízszintes, függőleges).
 */
public enum Axis {

    HORIZONTAL, VERTICAL;

    /**
     * Returns the axis perpendicular to this axis.
     */
    public Axis cross() {
        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }
}
