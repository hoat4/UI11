package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.graphics.effect.RoundedCorners;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMWidgetWrapper;

import java.util.List;

public class DOMRoundedCornersPeer extends DOMLayoutPeerBase {

    private final RoundedCorners widget;

    @Inject private Slot contentSlot;

    public DOMRoundedCornersPeer(RoundedCorners widget) {
        super(false, false);
        this.widget = widget;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
        elem().getClassList().add(DOMBoxPeer.CLASS_OVERFLOW_HIDDEN);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        elem().getStyle().setProperty("border-top-left-radius", widget.topLeftRadius().toString());
        elem().getStyle().setProperty("border-top-right-radius", widget.topRightRadius().toString());
        elem().getStyle().setProperty("border-bottom-right-radius", widget.bottomRightRadius().toString());
        elem().getStyle().setProperty("border-bottom-left-radius", widget.bottomLeftRadius().toString());

        return List.of(contentSlot.instantiate(new DOMWidgetWrapper(widget.content())).
                lookup(DOMElementHolder.class).element());
    }
}
