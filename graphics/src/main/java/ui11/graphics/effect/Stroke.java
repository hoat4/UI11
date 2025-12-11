package ui11.graphics.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.geom.Length;

public final class Stroke extends SubstitutedWidget {

    private final Widget texture;
    private final Length thickness;
    private final Path path;

    public Stroke(Widget texture, Length thickness, Path path) {
        this.texture = texture;
        this.thickness = thickness;
        this.path = path;
    }

    public Stroke(Color color, Length thickness, Path path) {
        this(new ColorFill(color), thickness, path);
    }

    public Widget texture() {
        return texture;
    }

    public Length thickness() {
        return thickness;
    }

    public Path path() {
        return path;
    }

    // TODO enum StrokeAlignment { INSIDE, OUTSIDE, CENTERED }
    //      enum LineJoin { BEVEL, MITER, ROUND }
    //      enum LineCap { BUTT, ROUND, SQUARE }
    // https://stackoverflow.com/questions/7241393/can-you-control-how-an-svgs-stroke-width-is-drawn
}
