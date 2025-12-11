package ui11.geom;

/**
 * Két dimenziós koordináta-rendszer tengelye (vízszintes, függőleges).
 */
public enum Axis {

    HORIZONTAL, VERTICAL;

    public Axis cross() {
        return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
    }
}
