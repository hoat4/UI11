package ui11.output.html;

import ui11.SubstitutedWidget;
import ui11.Widget;

public final class ClientSideElementWrapper extends SubstitutedWidget {

    private final Widget widget;

    public ClientSideElementWrapper(Widget widget) {
        this.widget = widget;
    }

    public Widget widget() {
        return widget;
    }
}
