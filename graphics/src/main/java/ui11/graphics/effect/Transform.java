package ui11.graphics.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Mat4;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class Transform extends SubstitutedWidget {

    @Nonnull private final Widget content;
    @Nonnull private final Mat4 transformation;

    public Transform(@Nonnull Widget content, @Nonnull Mat4 transformation) {
        Objects.requireNonNull(transformation);
        this.content = Objects.requireNonNull(content);
        this.transformation = Objects.requireNonNull(transformation);
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    public Mat4 transformation() {
        return transformation;
    }
}
