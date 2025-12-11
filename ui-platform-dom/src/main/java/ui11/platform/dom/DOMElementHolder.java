package ui11.platform.dom;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.provide.UpValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @param asCSSColor ha ez nem null, akkor lehet background-color vagy border-color értékébe
 *                  berakni ahelyett hogy DOM elemet generálnánk
 */
public record DOMElementHolder(@Nonnull HTMLElement element,
                               @Nullable String asCSSColor,
                               @Nullable String asCSSImage) implements UpValue {
}
