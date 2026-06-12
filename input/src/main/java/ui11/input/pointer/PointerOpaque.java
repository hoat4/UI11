package ui11.input.pointer;

import org.jspecify.annotations.NonNull;
import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;


/**
 * An empty widget, which doesn't let pointer events go through. It takes up minimal space what enabled by the
 * parent.
 * <p>
 * For an empty widget which is pointer transparent, use {@link ui11.graphics.Empty} instead.
 */
public class PointerOpaque extends SubstitutedWidget {

    private PointerOpaque() {
        if (INSTANCE != null)
            throw new Error();
    }

    private static final PointerOpaque INSTANCE = new PointerOpaque();

    public static @NonNull PointerOpaque pointerOpaque() {
        return INSTANCE;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new ColorFill(Color.TRANSPARENT);
    }

    @Override
    public String toString() {
        return "PointerOpaque";
    }
}
