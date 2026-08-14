package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.SubstitutedWidget;

/**
 * @param asCSSColor ha ez nem null, akkor lehet background-color vagy border-color értékébe berakni ahelyett hogy
 *                   DOM elemet generálnánk
 */
public record DOMElementHolder(
        @NonNull HTMLElement element,
        @Nullable String asCSSColor,
        @Nullable String asCSSImage
) {
    public boolean isHidden() {
        return "none".equals(element.getStyle().getPropertyValue("display"));
    }
}
