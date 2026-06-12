package ui11.graphics.effect;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

public class Clip extends SubstitutedWidget {

    private final Widget content;

    public Clip(Widget content) {
        this.content = content;
    }

    public Widget content() {
        return content;
    }
}
