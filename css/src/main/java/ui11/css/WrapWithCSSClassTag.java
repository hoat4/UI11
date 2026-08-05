package ui11.css;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class WrapWithCSSClassTag extends SubstitutedWidget {

    private final @NonNull String className;
    private final @NonNull Widget content;

    @Remember private Slot contentSlot;

    public WrapWithCSSClassTag(@NonNull String className, @NonNull Widget content) {
        this.className = Objects.requireNonNull(className);
        this.content = Objects.requireNonNull(content);
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

    @Override
    protected void initState() {
        contentSlot = new Slot();
    }

    @Override
    protected WrapWithCSSClassTag forSubstitution() {
        return new WrapWithCSSClassTag(
                className,
                contentSlot.with(content)
        );
    }

    public @NonNull String className() {
        return className;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
