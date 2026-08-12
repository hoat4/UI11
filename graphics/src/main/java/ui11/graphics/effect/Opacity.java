package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class Opacity extends SubstitutedWidget {

    private final double opacity;
    private final Widget content;

    public Opacity(double opacity, @NonNull Widget content) {
        if (opacity < 0 || opacity > 1)
            throw new IllegalArgumentException("invalid opacity: " + opacity);
        this.opacity = opacity;
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected Opacity forSubstitution() {
        return new Opacity(
                opacity,
                withID("content", content)
        );
    }

    public double opacity() {
        return opacity;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
