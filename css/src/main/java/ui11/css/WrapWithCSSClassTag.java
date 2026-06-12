package ui11.css;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class WrapWithCSSClassTag extends SubstitutedWidget {

    private final String className;
    private final @NonNull Widget content;

    public WrapWithCSSClassTag(String className, @NonNull Widget content) {
        Objects.requireNonNull(className);
        this.className = className;
        this.content = content;
    }

    public static Widget wrapWithCssClass(String className, Widget element) {
        if (element == null)
            return null;
        return new WrapWithCSSClassTag(className, element);
    }

    public static Widget wrapWithCssClass(String className1, String className2, Widget element) {
        if (element == null)
            return null;
        Objects.requireNonNull(className1);
        return CSSClassTag.cssClass(className2, new WrapWithCSSClassTag(className1, element));
    }

    public String className() {
        return className;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }
}
