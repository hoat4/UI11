package ui11.css;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.graphics.Empty;

import org.jspecify.annotations.NonNull;
import java.util.List;
import java.util.Objects;

// szándékosan nem List<String> a className, mert 1 classnév esetén ez a
// kompaktabb reprezentáció, 2 classnév esetén pedig ugyanannyi memóriát igényelnek, ha
// Listnek List12-t használunk. Sőt, ha ListN-t vagy ArrayListet, akkor valszeg 3 classnév
// esetén is valszeg kevésbé tömör, mint a List nélküli láncolt ábrázolás.
// Illetve így a konstruktorban nem kell foglalkozni a listák egymásba olvasztásával.

/**
 * @see WrapWithCSSClassTag
 */
public final class CSSClassTag extends SubstitutedWidget {

    private final String className;
    private final Widget content;

    public CSSClassTag(String className, Widget content) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(className);
        // lehetne azt is ellenőrizni hogy nem üres string-e
        this.className = className;
        this.content = content;
    }

    public String className() {
        return className;
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }

    public static Widget cssClass(String className, Widget widget) {
        if (className == null || widget == null)
            return widget;
        else
            return new CSSClassTag(className, widget);
    }

    public static Widget cssClass(String className1, String className2, Widget widget) {
        if (widget == null)
            return null;
        if (className2 != null)
            widget = cssClass(className2, widget);
        if (className1 != null)
            widget = cssClass(className1, widget);
        return widget;
    }

    public static Widget cssClass(String className1, String className2, String className3,
                                  Widget widget) {
        if (widget == null)
            return null;
        if (className3 != null)
            widget = cssClass(className3, widget);
        if (className2 != null)
            widget = cssClass(className2, widget);
        if (className1 != null)
            widget = cssClass(className1, widget);
        return widget;
    }

    public static Widget cssClass(List<String> classNames, Widget widget) {
        if (widget == null)
            return null;
        for (String s : classNames.reversed())
            widget = cssClass(s, widget);
        return widget;
    }

    public static Widget cssGraphic(String className) {
        return new CSSClassTag(className, Empty.empty());
    }

    public static Widget cssGraphic(String... classNames) {
        Widget w = Empty.empty();
        for (int i = classNames.length - 1; i >= 0; i--)
            if (classNames[i] != null)
                w = new CSSClassTag(classNames[i], w);
        return w;
    }
}
