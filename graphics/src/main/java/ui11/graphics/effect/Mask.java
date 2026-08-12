package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// TODO mask módok (luminosity, alpha, stb.)

public final class Mask extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull Widget mask;

    public Mask(@NonNull Widget content, @NonNull Widget mask) {
        this.content = Objects.requireNonNull(content);
        this.mask = Objects.requireNonNull(mask);
    }

    @Override
    protected Mask forSubstitution() {
        return new Mask(
                withID("content", content),
                withID("mask", mask)
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Widget mask() {
        return mask;
    }
}
