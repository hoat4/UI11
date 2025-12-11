package ui11.platform.dom;

import ui11.SubstitutedWidget;
import org.teavm.jso.dom.html.HTMLElement;

public final class DOMElementWidget extends SubstitutedWidget {

    private final HTMLElement domElement;

    public DOMElementWidget(HTMLElement domElement) {
        this.domElement = domElement;
    }

    public HTMLElement domElement() {
        return domElement;
    }

    @Override
    public String toString() {
        // különben TypeError: $obj.$toString is not a function
        return "DOMElementWidget (" + domElement.getNodeName() + ")";
    }
}
