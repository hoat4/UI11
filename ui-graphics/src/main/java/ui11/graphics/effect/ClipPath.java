package ui11.graphics.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ClipPath extends SubstitutedWidget {

    @Nonnull private final Widget content;
    @Nonnull private final Path shape;

    public ClipPath(@Nonnull Widget content, @Nonnull Path shape) {
        this.content = Objects.requireNonNull(content);
        this.shape = Objects.requireNonNull(shape);
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    public Path shape() {
        return shape;
    }
}
