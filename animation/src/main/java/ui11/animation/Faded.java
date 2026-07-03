package ui11.animation;

import org.jspecify.annotations.NonNull;
import ui11.*;
import ui11.graphics.effect.Opacity;
import ui11.input.pointer.PointerTransparent;

import java.time.Duration;
import java.util.Objects;

import static ui11.graphics.Empty.empty;

public class Faded extends Widget {

    private final @NonNull Widget content;
    private final boolean visible;

    @Inject private Slot contentSlot;

    // lehet hogy kétféle Tween kéne megjelenéshez és bezáráshoz.
    // persze akkor meg kérdés, hogyha megjelenés közben zárjuk be, akkor hogy nézzen ki a bezárás.

    public Faded(boolean visible, @NonNull Widget content) {
        this.visible = visible;
        this.content = content;
        Objects.requireNonNull(content);
    }

    @Override
    protected Widget build() {
        Widget content = this.content.withSlot(contentSlot);

        return new ValueSmoother<>(visible ? 1.0 : 0.0,
                Duration.ofMillis(300),
                Tween.ease(Tween.ofDouble()),
                animatedValue -> {
                    if (animatedValue == 0)
                        return empty();
                    else
                        return new Opacity(animatedValue, visible ? content : new PointerTransparent(content));
                });
    }
}
