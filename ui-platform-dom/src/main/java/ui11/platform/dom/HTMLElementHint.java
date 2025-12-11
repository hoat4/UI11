package ui11.platform.dom;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

// TODO ez most nincs rendesen implementálva, mindenképp új elemmel veszi körbe,
//      sose meglévő elemnevet helyettesít
public final class HTMLElementHint extends SubstitutedWidget {

    @Nonnull private final String htmlElementName;
    @Nonnull private final Widget content;

    public HTMLElementHint(String htmlElementName, Widget content) {
        this.htmlElementName = Objects.requireNonNull(htmlElementName);
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public String htmlElementName() {
        return htmlElementName;
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return content;
    }
}
