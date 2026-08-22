package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.platform.dom.DOMElementWidget;
import ui11.platform.dom.DOMPeerBase;

public class DOMElementWrapperPeer extends DOMPeerBase<HTMLElement> {

    private final DOMElementWidget domElementWrapper;

    public DOMElementWrapperPeer(DOMElementWidget domElementWrapper) {
        this.domElementWrapper = domElementWrapper;
    }

    @Override
    protected HTMLElement fixedElement() {
        return domElementWrapper.domElement();
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        return endingWidget();
    }
}