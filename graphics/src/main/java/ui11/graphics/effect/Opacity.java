package ui11.graphics.effect;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Opacity extends SubstitutedWidget {

    private final double opacity;
    private final Widget content;

    @Inject private Slot contentSlot;

    public Opacity(double opacity, @NonNull Widget content) {
        if (opacity < 0 || opacity > 1)
            throw new IllegalArgumentException("invalid opacity: " + opacity);
        this.opacity = opacity;
        this.content = Objects.requireNonNull(content);
    }

    public double opacity() {
        return opacity;
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
