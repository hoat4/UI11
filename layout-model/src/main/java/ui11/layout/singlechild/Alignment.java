package ui11.layout.singlechild;

public enum Alignment {

    LEFT(0, 1, 0, 0),
    HCENTER(0, 1, 0, 1),
    RIGHT(0, 0, 0, 1),
    TOP(0, 0, 1, 0),
    VCENTER(1, 0, 1, 0),
    BOTTOM(1, 0, 0, 0),
    LEFT_TOP(0, 1, 1, 0),
    CENTER_TOP(0, 1, 1, 1),
    RIGHT_TOP(0, 0, 1, 1),
    LEFT_CENTER(1, 1, 1, 0),
    CENTER(1, 1, 1, 1),
    RIGHT_CENTER(1, 0, 1, 1),
    LEFT_BOTTOM(1, 1, 0, 0),
    CENTER_BOTTOM(1, 1, 0, 1),
    RIGHT_BOTTOM(1, 0, 0, 1);

    public final double top;
    public final double right;
    public final double bottom;
    public final double left;
    public final double horizSum;
    public final double vertSum;
    public final double leftFraction;
    public final double topFraction;
    public final String displayName;

    Alignment(double top, double right, double bottom, double left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        this.horizSum = left + right;
        this.vertSum = top + bottom;

        /* horizSum == 0 illetve vertSum == 0 esetén mindegy, csak ne NaN legyen */
        this.leftFraction = horizSum == 0 ? 0 : left / horizSum;
        this.topFraction = vertSum == 0 ? 0 : top / vertSum;

        displayName = name().
                replace('_', ' ').
                replace("LEFT", "Left").
                replace("RIGHT", "Right").
                replace("TOP", "Top").
                replace("BOTTOM", "Bottom").
                replace("CENTER", "Center");
    }
}
