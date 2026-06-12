package ui11.layout.singlechild;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

public class Sticky extends SubstitutedWidget {

    private final Widget content;

    public Sticky(Widget content) {
        this.content = content;
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content; // TODO
    }
}
