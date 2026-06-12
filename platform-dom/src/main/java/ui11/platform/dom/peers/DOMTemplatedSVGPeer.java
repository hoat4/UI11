package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.Widget;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMPeerBase;

import java.util.Map;

public class DOMTemplatedSVGPeer extends DOMPeerBase<HTMLElement> {

    private final String svgMarkup;
    private final Map<String, ? extends Widget> embeddedWidgets;

    @Inject private MultiSlot<String> slots;

    public DOMTemplatedSVGPeer(String svgMarkup,
                               Map<String, ? extends Widget> embeddedWidgets) {
        this.svgMarkup = svgMarkup;
        this.embeddedWidgets = embeddedWidgets;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
    }

    @Override
    protected void update() {
        elem().setInnerHTML(svgMarkup);

        embeddedWidgets.forEach((id, w) -> {
            HTMLElement foreignObjectElement = elem().querySelector("#" + id);
            foreignObjectElement.getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
            foreignObjectElement.setInnerHTML("");
            HTMLElement e = peerOf(slots.get(id), w).element();
            DOMLayoutPeerBase.removeAllChildLayoutProperties(e);
            foreignObjectElement.appendChild(e);
        });
    }
}
