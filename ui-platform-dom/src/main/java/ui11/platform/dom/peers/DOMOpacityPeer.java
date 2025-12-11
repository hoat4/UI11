package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.graphics.effect.Opacity;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMWidgetWrapper;

import java.util.List;

// eredetileg DOMPeer kezelte Node.tmp_findTags-zal Opacity-t, de úgy egyrészt nem tudunk reagálni a változásokra,
// másrészt felesleges updateelni az egész peert, főleg ha valami nagy layout.
// cserébe így viszont plusz egy DOM elem lesz, ami lassú.
public class DOMOpacityPeer extends DOMLayoutPeerBase {

    private final Opacity opacity;

    @Inject private Slot contentSlot;

    public DOMOpacityPeer(Opacity opacity) {
        super(false, false);
        this.opacity = opacity;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMOverlayLayoutPeer.CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        elem().getStyle().setProperty("opacity", Double.toString(opacity.opacity()));

        return List.of(contentSlot.instantiate(new DOMWidgetWrapper(opacity.content())).
                lookup(DOMElementHolder.class).element());
    }
}
