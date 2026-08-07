package ui11.graphics.shaper;

import org.jspecify.annotations.NonNull;
import ui11.Key;
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

    @Remember private Key textureSlot;

    public Stroke(@NonNull Widget texture, @NonNull Length thickness, @NonNull Path path) {
        this.texture = Objects.requireNonNull(texture);
        this.thickness = Objects.requireNonNull(thickness);
        this.path = Objects.requireNonNull(path);

        if (thickness.isRelative())
            throw new RuntimeException("relative size not supported for stroke thickness: " + thickness);
    }

    public Stroke(@NonNull Color color, @NonNull Length thickness, @NonNull Path path) {
        this(new ColorFill(color), thickness, path);
    }

    @Override
    protected void initState() {
        textureSlot = Key.create();
    }

    @Override
    protected Stroke forSubstitution() {
        return new Stroke(
                texture.withKey(textureSlot),
                thickness,
                path
        );
    }

    public @NonNull Widget texture() {
        return texture;
    }

    public @NonNull Length thickness() {
        return thickness;
    }

    public @NonNull Path path() {
        return path;
    }

    // TODO enum StrokeAlignment { INSIDE, OUTSIDE, CENTERED }
    //      enum LineJoin { BEVEL, MITER, ROUND }
    //      enum LineCap { BUTT, ROUND, SQUARE }
    // https://stackoverflow.com/questions/7241393/can-you-control-how-an-svgs-stroke-width-is-drawn
}
