package ui11.decoration;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.color.Color;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;

public final class BoxShadow extends SubstitutedWidget {

    private final Color color;
    private final Length blur;
    private final Length xOffset;
    private final Length yOffset;
    private final Length spread;
    private final Widget content;

    public BoxShadow(Color color, Length blur, Length xOffset, Length yOffset, Length spread,
                     Widget content) {
        this.color = color;
        this.blur = blur;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.spread = spread;
        this.content = content;
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

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new Box(content).withBoxShadow(new Box.BoxShadow(color, blur, xOffset, yOffset, spread));
    }
}
