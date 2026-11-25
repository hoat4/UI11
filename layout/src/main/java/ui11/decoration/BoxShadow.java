package ui11.decoration;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.color.Color;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

public final class BoxShadow extends SubstitutedWidget {

    private final Color color;
    private final Length blur;
    private final Length xOffset;
    private final Length yOffset;
    private final Length spread;
    private final @NonNull Widget content;

    public BoxShadow(Color color, Length blur, Length xOffset, Length yOffset, Length spread,
                     @NonNull Widget content) {
        // TODO többi is nemnull gondolom
        this.color = color;
        this.blur = blur;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.spread = spread;
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected BoxShadow forSubstitution() {
        return new BoxShadow(color, blur, xOffset, yOffset, spread, withID("content", content));
    }

    public Color color() {
        return color;
    }

    public Length blur() {
        return blur;
    }

    public Length xOffset() {
        return xOffset;
    }

    public Length yOffset() {
        return yOffset;
    }

    public Length spread() {
        return spread;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new Box(content()).withBoxShadow(new Box.BoxShadow(color, blur, xOffset, yOffset, spread));
    }
}
