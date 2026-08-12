package ui11.graphics.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Mat4;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Transform extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Mat4 transformation;

    public Transform(@NonNull Widget content, @NonNull Mat4 transformation) {
        this.content = Objects.requireNonNull(content);
        this.transformation = Objects.requireNonNull(transformation);
    }

    @Override
    protected Transform forSubstitution() {
        return new Transform(
                withID("content", content),
                transformation
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Mat4 transformation() {
        return transformation;
    }
}
