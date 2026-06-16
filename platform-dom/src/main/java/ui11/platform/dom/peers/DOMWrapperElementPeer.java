package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMPeerBase;
import ui11.platform.dom.HTMLElementHint;

public class DOMWrapperElementPeer extends DOMPeerBase<HTMLElement> {

    private final HTMLElementHint tag;

    // TODO ez nem tud reagálni a htmlElementName megváltozására
    public DOMWrapperElementPeer(HTMLElementHint tag) {
        this.tag = tag;
    }

    @Override
    protected String elementName() {
        return tag.htmlElementName();
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
    }

    @Override
    protected Widget doBuild() {
        return makePeer(tag.content(), peer->{
            HTMLElement childElement = peer.element();

            DOMLayoutPeerBase.removeAllChildLayoutProperties(childElement);
            if (elem().getChildren().getLength() == 0)
                elem().appendChild(childElement);
            else if (elem().getChildren().item(0) != childElement) {
                elem().setInnerHTML("");
                elem().appendChild(childElement);
            }
            return endingWidget();
        });
    }
}
