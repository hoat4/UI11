package ui11.layout.singlechild;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

/**
 * Hagyja, hogy a szülő beállítson bármilyen magasságot, viszont a preferált szélességet úgy határozza meg, hogy
 * megfeleljen az elem preferált aspect ratiojának és a szülő által meghatározott magasságnak is.
 */
public final class PassiveHeight extends SubstitutedWidget {

    private final Widget content;
    private final double aspectRatio;

    public PassiveHeight(Widget content, double aspectRatio) {
        if (aspectRatio < 0 && aspectRatio != -1 || !Double.isFinite(aspectRatio))
            throw new IllegalArgumentException();
        this.content = content;
        this.aspectRatio = aspectRatio;
    }

    public PassiveHeight(Widget content) {
        this(content, -1);
    }

    public Widget content() {
        return content;
    }

    public double aspectRatio() {
        return aspectRatio;
    }
}
