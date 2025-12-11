package ui11.platform.dom.peers;

import ui11.Slot;
import ui11.platform.dom.*;
import org.teavm.jso.dom.html.HTMLElement;

public class DOMWrapperElementPeer extends DOMPeerBase<HTMLElement> {

    private final HTMLElementHint tag;

    @Inject private Slot contentSlot;

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
    protected void update() {
        HTMLElement childElement = contentSlot.instantiate(new DOMWidgetWrapper(tag.content())).
                lookup(DOMElementHolder.class).element();

        DOMLayoutPeerBase.removeAllChildLayoutProperties(childElement);
        if (elem().getChildren().getLength() == 0)
            elem().appendChild(childElement);
        else if (elem().getChildren().item(0) != childElement) {
            elem().setInnerHTML("");
            elem().appendChild(childElement);
        }
    }
}
