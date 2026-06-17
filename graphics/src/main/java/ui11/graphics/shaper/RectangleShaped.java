package ui11.graphics.shaper;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Rect;
import ui11.geom.Size;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class RectangleShaped extends SubstitutedWidget {

    private final Widget content;
    private final Size shape;

    public RectangleShaped(Widget content, Size shape) {
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

    @Override
    protected @NonNull Widget fallbackContent() {
        return new PathShaped(content, Path.ofRect(Rect.of(shape)));
    }
}
