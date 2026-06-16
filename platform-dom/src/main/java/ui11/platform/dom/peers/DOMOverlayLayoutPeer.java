package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.graphics.effect.Overlay;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;

public class DOMOverlayLayoutPeer extends DOMLayoutPeerBase {

    static final String CLASS_SYMMETRIC_OVERLAY_GRID = "gQ";

    private final Overlay overlayLayout;

    public DOMOverlayLayoutPeer(Overlay overlayLayout) {
        super(false, false);
        this.overlayLayout = overlayLayout;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected Widget doBuild() {
        return makePeers(overlayLayout.items(), hList -> {
            return updateChildren(hList.stream().map(DOMElementHolder::element).toList());
        });
    }
}
