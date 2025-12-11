package ui11.graphics.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Rect;
import ui11.geom.Size;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ClipRect extends SubstitutedWidget {
    private final Widget content;
    private final Size shape;

    public ClipRect(Widget content, Size shape) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(shape);
        this.content = content;
        this.shape = shape;
    }

    public Widget content() {
        return content;
    }

    public Size shape() {
        return shape;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new ClipPath(content, Path.ofRect(Rect.of(shape)));
    }
}
