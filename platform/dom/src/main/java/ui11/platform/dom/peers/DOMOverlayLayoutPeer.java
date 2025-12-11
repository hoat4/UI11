package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.MultiSlot;
import ui11.Widget;
import ui11.WidgetInstantiation;
import ui11.graphics.effect.Overlay;
import ui11.layout.Gone;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMWidgetWrapper;

import java.util.ArrayList;
import java.util.List;

public class DOMOverlayLayoutPeer extends DOMLayoutPeerBase {

    static final String CLASS_SYMMETRIC_OVERLAY_GRID = "gQ";

    private final Overlay overlayLayout;

    @Inject private MultiSlot<Integer> slots;

    public DOMOverlayLayoutPeer(Overlay overlayLayout) {
        super(false, false);
        this.overlayLayout = overlayLayout;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        List<HTMLElement> childElements = new ArrayList<>();
        List<? extends Widget> widgets = overlayLayout.items();
        for (int i = 0; i < widgets.size(); i++) {
            Widget widget = widgets.get(i);
            WidgetInstantiation childH = slots.instantiate(i, new DOMWidgetWrapper(widget));
            if (childH.lookupOptional(Gone.class).isEmpty())
                childElements.add(childH.lookup(DOMElementHolder.class).element());
        }
        return childElements;
    }
}
