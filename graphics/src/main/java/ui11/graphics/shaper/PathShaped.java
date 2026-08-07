package ui11.graphics.shaper;

import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Path;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class PathShaped extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Path shape;

    @Remember private Key contentKey;

    public PathShaped(@NonNull Widget content, @NonNull Path shape) {
        this.content = Objects.requireNonNull(content);
        this.shape = Objects.requireNonNull(shape);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected PathShaped forSubstitution() {
        return new PathShaped(
                content.withKey(contentKey),
                shape
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Path shape() {
        return shape;
    }
}
