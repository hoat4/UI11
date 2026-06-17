package ui11.graphics.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Opacity extends SubstitutedWidget {

    private final double opacity;
    private final Widget content;

    public Opacity(double opacity, Widget content) {
        Objects.requireNonNull(content);
        if (opacity < 0 || opacity > 1)
            throw new IllegalArgumentException("invalid opacity: " + opacity);
        this.opacity = opacity;
        this.content = content;
    }

    public double opacity() {
        return opacity;
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }
}
