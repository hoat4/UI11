package ui11.graphics.effect;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

// TODO mask módok (luminosity, alpha, stb.)

public final class Mask extends SubstitutedWidget {

    private final Widget content;
    private final Widget mask;

    public Mask(Widget content, Widget mask) {
        this.content = content;
        this.mask = mask;
    }

    public Widget content() {
        return content;
    }

    public Widget mask() {
        return mask;
    }
}
