package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.EndingWidget;

public final class DOMElementHolder extends EndingWidget {

    private final @NonNull HTMLElement element;
    private final @Nullable String asCSSColor;
    private final @Nullable String asCSSImage;

    /**
     * @param asCSSColor ha ez nem null, akkor lehet background-color vagy border-color értékébe berakni ahelyett hogy
     *                   DOM elemet generálnánk
     */
    public DOMElementHolder(@NonNull HTMLElement element,
                            @Nullable String asCSSColor,
                            @Nullable String asCSSImage) {
        this.element = element;
        this.asCSSColor = asCSSColor;
        this.asCSSImage = asCSSImage;
    }

    // TODO ha Hidden/Gone, akkor nem is kéne HTMLElementet létrehozni

    public @NonNull HTMLElement element() {
        return element;
    }

    public @Nullable String asCSSColor() {
        return asCSSColor;
    }

    public @Nullable String asCSSImage() {
        return asCSSImage;
    }

    public boolean isHidden() {
        return "none".equals(element.getStyle().getPropertyValue("display"));
    }
}
