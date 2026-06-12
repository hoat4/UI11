package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.Widget;
import ui11.platform.dom.DOMPeerBase;
import ui11.text.formatted.OrderedList;

import java.util.List;

public class DOMOrderedListPeer extends DOMPeerBase<HTMLElement> {

    private final OrderedList orderedList;

    @Inject private MultiSlot<Integer> slots;

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
    protected void update() {
        elem().setInnerHTML("");
        // TODO diff
        List<? extends Widget> items = orderedList.items();
        for (int i = 0; i < items.size(); i++) {
            Widget w = items.get(i);
            HTMLElement li = elem().getOwnerDocument().createElement("li");
            li.appendChild(peerOf(slots.get(i), w).element());
            elem().appendChild(li);
        }
    }
}
