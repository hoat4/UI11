package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.Widget;
import ui11.graphics.effect.Mask;
import ui11.platform.dom.*;

import java.util.List;

public class DOMMaskPeer extends DOMLayoutPeerBase {

    private final Mask maskWidget;

    public DOMMaskPeer(Mask mask) {
        super(false, false);
        this.maskWidget = mask;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMOverlayLayoutPeer.CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected Widget doBuild() {
        return makePeer(maskWidget.mask(), maskPeer -> {
            final String cssImage = maskPeer.asCSSImage();
            if (cssImage == null)
                throw new RuntimeException("unsupported for masks: " + maskWidget.mask());

            elem().getStyle().setProperty("mask-image", cssImage);
            elem().getStyle().setProperty("-webkit-mask-image", cssImage);

            return updateToSingleChild(maskWidget.content());
        });
    }
}
