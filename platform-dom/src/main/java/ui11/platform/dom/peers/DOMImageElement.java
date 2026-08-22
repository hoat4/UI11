package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;
import ui11.Widget;
import ui11.platform.dom.DOMPeerBase;

public class DOMImageElement extends DOMPeerBase<HTMLElement> {

    private final String src;
    private final boolean interactive;

    public DOMImageElement(String srcURL) {
        this.src = srcURL;
        this.interactive = false;
    }

    public DOMImageElement(String srcURL, boolean interactive) {
        this.src = srcURL;
        this.interactive = interactive;
    }

    @Override
    protected String elementName() {
        return interactive ? "object" : "img";
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        if (interactive)
            elem().setAttribute("data", src);
        else
            ((HTMLImageElement) elem()).setSrc(src);
        return endingWidget();
    }
}
