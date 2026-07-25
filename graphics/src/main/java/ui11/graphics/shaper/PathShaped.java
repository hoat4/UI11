package ui11.graphics.shaper;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class PathShaped extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Path shape;

    @Inject private Slot contentSlot;

    public PathShaped(@NonNull Widget content, @NonNull Path shape) {
        this.content = Objects.requireNonNull(content);
        this.shape = Objects.requireNonNull(shape);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public @NonNull Path shape() {
        return shape;
    }
}
