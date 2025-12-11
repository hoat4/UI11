package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.Slot;
import ui11.document.TemplatedSVG;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMPeerBase;
import ui11.platform.dom.DOMWidgetWrapper;

public class DOMTemplatedSVGPeer extends DOMPeerBase<HTMLElement> {

    private final TemplatedSVG templatedSVG;

    @Inject private MultiSlot<String> slots;

    public DOMTemplatedSVGPeer(TemplatedSVG templatedSVG) {
        this.templatedSVG = templatedSVG;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
    }

    @Override
    protected void update() {
        elem().setInnerHTML(templatedSVG.svg().source());

        templatedSVG.embeddedWidgets().forEach((id, w) -> {
            HTMLElement foreignObjectElement = elem().querySelector("#" + id);
            foreignObjectElement.getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
            foreignObjectElement.setInnerHTML("");
            HTMLElement e = slots.instantiate(id, new DOMWidgetWrapper(w)).
                    lookup(DOMElementHolder.class).element();
            DOMLayoutPeerBase.removeAllChildLayoutProperties(e);
            foreignObjectElement.appendChild(e);
        });
    }
}
