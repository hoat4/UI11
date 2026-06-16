package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.graphics.shaper.RoundedCorners;
import ui11.platform.dom.DOMLayoutPeerBase;

public class DOMRoundedCornersPeer extends DOMLayoutPeerBase {

    private final RoundedCorners widget;

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
    protected Widget doBuild() {
        elem().getStyle().setProperty("border-top-left-radius", widget.topLeftRadius().toString());
        elem().getStyle().setProperty("border-top-right-radius", widget.topRightRadius().toString());
        elem().getStyle().setProperty("border-bottom-right-radius", widget.bottomRightRadius().toString());
        elem().getStyle().setProperty("border-bottom-left-radius", widget.bottomLeftRadius().toString());

        return updateToSingleChild(widget.content());
    }
}
