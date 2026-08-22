package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLIFrameElement;
import ui11.Widget;
import ui11.platform.dom.DOMPeerBase;
import ui11.webcontent.WebContentFrame;

public class WebContentFramePeer extends DOMPeerBase<HTMLIFrameElement> {

    private final WebContentFrame webContentFrame;

    public WebContentFramePeer(WebContentFrame webContentFrame) {
        this.webContentFrame = webContentFrame;
    }

    @Override
    protected String elementName() {
        return "iframe";
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        elem().setSourceAddress(webContentFrame.url().toString());
        return endingWidget();
    }
}
