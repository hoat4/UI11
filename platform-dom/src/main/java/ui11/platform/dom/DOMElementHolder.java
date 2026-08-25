package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.SubstitutedWidget;

/**
 * @param asCSSColor ha ez nem null, akkor lehet background-color vagy border-color értékébe berakni ahelyett hogy
 *                   DOM elemet generálnánk
 */
public final class DOMElementHolder extends SubstitutedWidget {
    public final @NonNull HTMLElement element;
    public final @Nullable String asCSSColor;
    public final @Nullable String asCSSImage;

    public DOMElementHolder(@NonNull HTMLElement element, @Nullable String asCSSColor, @Nullable String asCSSImage) {
        this.element = element;
        this.asCSSColor = asCSSColor;
        this.asCSSImage = asCSSImage;
    }

    public boolean isHidden() {
        return "none".equals(element.getStyle().getPropertyValue("display"));
    }

    public @NonNull HTMLElement element() {
        return element;
    }

    public @Nullable String asCSSColor() {
        return asCSSColor;
    }

    public @Nullable String asCSSImage() {
        return asCSSImage;
    }
}
