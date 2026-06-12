package ui11.decoration;

import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;

import static ui11.geom.Length.px;

public final class HLine extends Widget {

    private final Widget fill;

    public HLine(Widget fill) {
        this.fill = fill;
    }

    public HLine(Color color) {
        this(new ColorFill(color));
    }

    // TODO valszeg Align.vcenterbe kéne wrappelni, valamint paraméternek kéne lennie thicknessnek

    @Override
    protected Widget build() {
        return new Box(fill).withFixedSize(null, px(1));
    }

    @Override
    public String toString() {
        return "HLine[" +
                "fill=" + fill + ']';
    }
}
