package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLAnchorElement;
import ui11.Slot;
import ui11.Widget;
import ui11.control.Hyperlink;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMPeerBase;

public class DOMHyperlinkPeer extends DOMPeerBase<HTMLAnchorElement> {

    private final Hyperlink hyperlink;

    @Inject private Slot contentSlot;

    public DOMHyperlinkPeer(Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
    }

    @Override
    protected String elementName() {
        return "a";
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected void update() {
        elem().setHref(hyperlink.target().toString());

        DOMElementHolder childPeer = peerOf_sameSurface(contentSlot, hyperlink.content());
        if (elem().getChildren().getLength() != 1 || !elem().getChildren().item(0).equals(childPeer.element())) {
            elem().setInnerHTML("");
            elem().appendChild(childPeer.element());
        }
    }
}
