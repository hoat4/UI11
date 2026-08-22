package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.platform.dom.DOMPeerBase;

public class EmptyElementPeer extends DOMPeerBase<HTMLElement> {

    @Override
    protected void initElement() {
        elem().getStyle().setProperty("pointer-events", "none");
    }

    @Override
    protected Widget doBuild() {
        return endingWidget();
    }
}
