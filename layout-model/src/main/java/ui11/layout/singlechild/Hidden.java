package ui11.layout.singlechild;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Layout és input szempontból is rejtett lesz, nem csak grafikailag
 * (tehát kb. mint Androidban GONE, CSS-ben display:none).
 */
// TODO leírni hogy mi a különbség Gone-hoz képest
public final class Hidden extends SubstitutedWidget {

    @Nonnull private final Widget content;

    // egyelőre csak abban különbözik attól ha csak child lenne de widget fában nem szerepl, hogy
    // scroll pozíció megőrződik.

    public Hidden(@Nonnull Widget content)  {
        this.content = content;
        Objects.requireNonNull(content);
    }

    @Nonnull
    public Widget content() {
        return content;
    }
}
