package ui11.platform.dom.peers;

import ui11.Slot;
import ui11.Widget;
import ui11.layout.singlechild.Padding;
import ui11.platform.dom.DOMLayoutPeerBase;

import java.util.List;

public class DOMPaddingPeer extends DOMLayoutPeerBase {

    private final Padding padding;

    public DOMPaddingPeer(Padding padding) {
        super(false, false);
        this.padding = padding;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMBoxPeer.CLASS_WRAPPERELEMENT);
    }

    @Override
    protected Widget doBuild() {
        if (padding.insets().isZero())
            elem().getStyle().removeProperty("padding");
        else
            elem().getStyle().setProperty("padding", insetsToCSS(padding.insets()));

        return makePeer(padding.content(), h->{
            return updateChildren(List.of(h.element()));
        });
    }
}
