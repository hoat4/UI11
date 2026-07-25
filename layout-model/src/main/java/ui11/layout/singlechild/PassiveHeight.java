package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

/**
 * Hagyja, hogy a szülő beállítson bármilyen magasságot, viszont a preferált szélességet úgy határozza meg, hogy
 * megfeleljen az elem preferált aspect ratiojának és a szülő által meghatározott magasságnak is.
 */
public final class PassiveHeight extends SubstitutedWidget {

    private final Widget content;
    private final double aspectRatio;

    @Inject private Slot contentSlot;

    public PassiveHeight(@NonNull Widget content, double aspectRatio) {
        if (aspectRatio < 0 && aspectRatio != -1 || !Double.isFinite(aspectRatio))
            throw new IllegalArgumentException();
        this.content = Objects.requireNonNull(content);
        this.aspectRatio = aspectRatio;
    }

    public PassiveHeight(Widget content) {
        this(content, -1);
    }

    public Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public double aspectRatio() {
        return aspectRatio;
    }
}
