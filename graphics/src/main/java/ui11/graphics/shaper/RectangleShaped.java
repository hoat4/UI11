package ui11.graphics.shaper;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;
import ui11.geom.Rect;
import ui11.geom.Size;

import java.util.Objects;

public final class RectangleShaped extends SubstitutedWidget {

    private final Widget content;
    private final Size shape;

    public RectangleShaped(@NonNull Widget content, @NonNull Size shape) {
        this.content = Objects.requireNonNull(content);
        this.shape = Objects.requireNonNull(shape);
    }

    @Override
    protected RectangleShaped forSubstitution() {
        return new RectangleShaped(
                withID("content", content),
                shape
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Size shape() {
        return shape;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new PathShaped(content(), Path.ofRect(Rect.of(shape)));
    }
}
