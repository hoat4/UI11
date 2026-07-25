package ui11.graphics.shaper;

import ui11.Slot;
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

    @Inject private Slot contentSlot;

    public RectangleShaped(@NonNull Widget content, @NonNull Size shape) {
        this.content = Objects.requireNonNull(content);
        this.shape = Objects.requireNonNull(shape);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public @NonNull Size shape() {
        return shape;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new PathShaped(content(), Path.ofRect(Rect.of(shape)));
    }
}
