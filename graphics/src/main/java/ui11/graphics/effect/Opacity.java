package ui11.graphics.effect;

import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class Opacity extends SubstitutedWidget {

    private final double opacity;
    private final Widget content;

    @Remember private Slot2 contentSlot;

    public Opacity(double opacity, @NonNull Widget content) {
        if (opacity < 0 || opacity > 1)
            throw new IllegalArgumentException("invalid opacity: " + opacity);
        this.opacity = opacity;
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Opacity forSubstitution() {
        return new Opacity(
                opacity,
                contentSlot.with(content)
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
