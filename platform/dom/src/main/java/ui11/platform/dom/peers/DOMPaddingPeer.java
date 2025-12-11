package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.layout.singlechild.Padding;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMWidgetWrapper;

import java.util.List;

public class DOMPaddingPeer extends DOMLayoutPeerBase {

    private final Padding padding;

    @Inject private Slot contentSlot;

    public DOMPaddingPeer(Padding padding) {
        super(false, false);
        this.padding = padding;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        if (padding.insets().isZero())
            elem().getStyle().removeProperty("padding");
        else
            elem().getStyle().setProperty("padding", insetsToCSS(padding.insets()));

        return List.of(contentSlot.instantiate(new DOMWidgetWrapper(padding.content())).
                lookup(DOMElementHolder.class).element());
    }
}
