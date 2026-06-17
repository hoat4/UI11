package ui11.graphics.shaper;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.geom.Length;

import java.util.Objects;

// TODO path nem kéne ide. de akkor ClipPathnak kéne tudnia kezelni nem zárt pathokat is.

public final class Stroke extends SubstitutedWidget {

    private final Widget texture;
    private final Length thickness;
    private final Path path;

    public Stroke(Widget texture, Length thickness, Path path) {
        this.texture = Objects.requireNonNull(texture);
        this.thickness = Objects.requireNonNull(thickness);
        this.path = Objects.requireNonNull(path);

        if (thickness.isRelative())
            throw new RuntimeException("relative size not supported for stroke thickness: " + thickness);
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
