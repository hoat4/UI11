package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class Clip extends SubstitutedWidget {

    private final Widget content;

    public Clip(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected Clip forSubstitution() {
        return new Clip(withID("content", content));
    }

    public @NonNull Widget content() {
        return content;
    }
}
