package ui11.control;

import ui11.Widget;
import ui11.geom.Vec2;
import ui11.observable.SimpleScope;
import ui11.control.menu.PopupMenuOpener;
import ui11.geom.Rect;
import ui11.input.gesture.ClickListener;

import java.util.function.Function;

public interface ButtonTarget {

    Widget wrapButton(Widget button);

    static ButtonTarget of(Runnable runnable) {
        return c -> new ClickListener(c, runnable);
    }

    static ButtonTarget ofPopup(Function<Rect, Vec2> popupPos, Function<SimpleScope, ? extends Widget> popup) {
        return c -> new PopupMenuOpener(c, popup, popupPos);
    }
}
