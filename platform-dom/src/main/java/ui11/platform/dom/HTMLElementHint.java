package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// TODO ez most nincs rendesen implementálva, mindenképp új elemmel veszi körbe,
//      sose meglévő elemnevet helyettesít
public final class HTMLElementHint extends SubstitutedWidget {

    private final @NonNull String htmlElementName;
    private final @NonNull Widget content;

    @Remember private Slot2 contentSlot;

    public HTMLElementHint(String htmlElementName, Widget content) {
        this.htmlElementName = Objects.requireNonNull(htmlElementName);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    public @NonNull String htmlElementName() {
        return htmlElementName;
    }

    @Override
    protected HTMLElementHint forSubstitution() {
        return new HTMLElementHint(htmlElementName, contentSlot.with(content));
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
