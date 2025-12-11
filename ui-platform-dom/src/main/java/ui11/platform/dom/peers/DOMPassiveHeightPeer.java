package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.document.URLImageView;
import ui11.layout.singlechild.PassiveHeight;
import ui11.platform.dom.*;

import java.net.URI;
import java.util.List;

import static ui11.css.CSSClassTag.cssClass;

public class DOMPassiveHeightPeer extends DOMLayoutPeerBase {

    private static final String CLASS_PASSIVE_HEIGHT_HELPER_IMAGE = "PH";

    // TODO meg kéne nézni, hogy ez átlátszó-e
    private static final URI IMAGE_1x1 = URI.create("data:image/gif;base64," +
            "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    private final PassiveHeight passiveHeight;

    @Inject private Slot contentSlot;
    @Inject private Slot helperImageSlot;

    public DOMPassiveHeightPeer(PassiveHeight passiveHeight) {
        super(false, false);
        this.passiveHeight = passiveHeight;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(DOMOverlayLayoutPeer.CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected List<? extends HTMLElement> children() {
        if (passiveHeight.aspectRatio() != 1)
            // ha megadott aspect ratio van, akkor SVG-t lehetne generálni, amit img src-be rakunk data URI-ként.
            // ha a child elem aspect ratiot kéne figyelmi (aspectRatio=-1), akkor nem tudom, mit lehetne csinálni.
            throw new RuntimeException("TODO PassiveHeight aspectRatio != 1");

        return List.of(
                helperImageSlot.instantiate(new DOMWidgetWrapper(cssClass(CLASS_PASSIVE_HEIGHT_HELPER_IMAGE,
                        new URLImageView(IMAGE_1x1)))).lookup(DOMElementHolder.class).element(),
                contentSlot.instantiate(new DOMWidgetWrapper(passiveHeight.content())).
                        lookup(DOMElementHolder.class).element()
        );
    }
}
