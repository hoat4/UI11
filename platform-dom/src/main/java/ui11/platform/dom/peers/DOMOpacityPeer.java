package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.graphics.effect.Opacity;
import ui11.platform.dom.DOMLayoutPeerBase;

// eredetileg DOMPeer kezelte Node.tmp_findTags-zal Opacity-t, de úgy egyrészt nem tudunk reagálni a változásokra,
// másrészt felesleges updateelni az egész peert, főleg ha valami nagy layout.
// cserébe így viszont plusz egy DOM elem lesz, ami lassú.
public class DOMOpacityPeer extends DOMLayoutPeerBase {

    private final Opacity opacity;

    public DOMOpacityPeer(Opacity opacity) {
        super(false, false);
        this.opacity = opacity;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMOverlayLayoutPeer.CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected Widget doBuild() {
        elem().getStyle().setProperty("opacity", Double.toString(opacity.opacity()));

        return updateToSingleChild(opacity.content());
    }
}
