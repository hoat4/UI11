package ui11.animation;

import ui11.*;
import ui11.graphics.effect.Opacity;
import ui11.input.pointer.PointerTransparent;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Objects;

import static ui11.graphics.Empty.empty;

public class Faded extends Widget {

    @Nonnull private final Widget content;
    private final boolean visible;

    @Inject private Slot contentSlot;

    @State private ValueSmoother<Double> t;

    // lehet hogy kétféle Tween kéne megjelenéshez és bezáráshoz.
    // persze akkor meg kérdés, hogyha megjelenés közben zárjuk be, akkor hogy nézzen ki a bezárás.

    public Faded(boolean visible, @Nonnull Widget content) {
        this.visible = visible;
        this.content = content;
        Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        t = new ValueSmoother<>(Duration.ofMillis(300), Tween.ease(Tween.ofDouble()));
    }

    @Override
    protected Widget build() {
        useComponent(t);

        Widget content = contentSlot.use(this.content);

        double animatedValue = t.value(visible ? 1.0 : 0.0);
        if (animatedValue == 0)
            return empty();
        else
            return new Opacity(animatedValue, visible ? content : new PointerTransparent(content));
    }
}
