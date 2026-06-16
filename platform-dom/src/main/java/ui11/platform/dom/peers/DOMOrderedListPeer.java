package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMPeerBase;
import ui11.text.formatted.OrderedList;

public class DOMOrderedListPeer extends DOMPeerBase<HTMLElement> {

    private final OrderedList orderedList;

    public DOMOrderedListPeer(OrderedList orderedList) {
        this.orderedList = orderedList;
    }

    @Override
    protected String elementName() {
        return "ol";
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        elem().setInnerHTML("");
        return makePeers(orderedList.items(), peerList-> {
            // TODO diff
            for (DOMElementHolder h : peerList) {
                HTMLElement li = elem().getOwnerDocument().createElement("li");
                li.appendChild(h.element());
                elem().appendChild(li);
            }
            return endingWidget();
        });
    }
}
