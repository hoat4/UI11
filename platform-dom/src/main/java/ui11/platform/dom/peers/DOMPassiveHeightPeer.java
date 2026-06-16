package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.layout.singlechild.PassiveHeight;
import ui11.platform.dom.DOMLayoutPeerBase;

import java.util.List;

import static ui11.css.CSSClassTag.cssClass;

public class DOMPassiveHeightPeer extends DOMLayoutPeerBase {

    private static final String CLASS_PASSIVE_HEIGHT_HELPER_IMAGE = "PH";

    // TODO meg kéne nézni, hogy ez átlátszó-e
    private static final String IMAGE_1x1 =
            "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";

    private final PassiveHeight passiveHeight;

    public DOMPassiveHeightPeer(PassiveHeight passiveHeight) {
        super(false, false);
        this.passiveHeight = passiveHeight;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMOverlayLayoutPeer.CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected Widget doBuild() {
        if (passiveHeight.aspectRatio() != 1)
            // ha megadott aspect ratio van, akkor SVG-t lehetne generálni, amit img src-be rakunk data URI-ként.
            // ha a child elem aspect ratiot kéne figyelmi (aspectRatio=-1), akkor nem tudom, mit lehetne csinálni.
            throw new RuntimeException("TODO PassiveHeight aspectRatio != 1: " + passiveHeight);

        Widget helperImage = cssClass(CLASS_PASSIVE_HEIGHT_HELPER_IMAGE,
                new DOMImageElement(IMAGE_1x1));
        return makePeer(helperImage, helperImageH -> {
            return makePeer(passiveHeight.content(), contentH -> {
                return updateChildren(List.of(helperImageH.element(), contentH.element()));
            });
        });
    }
}
