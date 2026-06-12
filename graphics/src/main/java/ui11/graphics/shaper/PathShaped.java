package ui11.graphics.shaper;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class PathShaped extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Path shape;

    public PathShaped(@NonNull Widget content, @NonNull Path shape) {
        this.content = Objects.requireNonNull(content);
        this.shape = Objects.requireNonNull(shape);
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Path shape() {
        return shape;
    }
}
