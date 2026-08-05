package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.decoration.Box;
import ui11.decoration.Box.BorderSpec;
import ui11.decoration.Box.BoxShadow;
import ui11.geom.Length;
import ui11.layout.Insets;
import ui11.platform.dom.DOMElementHolder;
import ui11.platform.dom.DOMLayoutPeerBase;
import ui11.platform.dom.DOMPeerBase;

import java.util.HashMap;
import java.util.Map;

import static ui11.css.CSSClassTag.cssClass;

// TODO withCorners(withBorder(...)) nem jól kombinálódik
public class DOMBoxPeer extends DOMPeerBase<HTMLElement> {

    public static final String CLASS_POSITION_RELATIVE = "p";
    public static final String CLASS_POSITION_ABSOLUTE = "P";
    public static final String CLASS_Z_ZERO = "Pz";
    public static final String CLASS_WRAPPERELEMENT = "we";
    public static final String CLASS_OVERFLOW_HIDDEN = "oH";
    public static final String CLASS_IMG_IN_WRAPPER_ELEMENT = "wI";

    private final Box box;

    public DOMBoxPeer(Box box) {
        this.box = box;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_WRAPPERELEMENT);
    }

    @Override
    protected Widget doBuild() {
        enum ChildType {
            CONTENT, BACKGROUND, BORDER_FILL
        }

        Map<ChildType, Widget> widgets = new HashMap<>();
        widgets.put(ChildType.CONTENT, box.content());
        if (box.background() != null)
            widgets.put(ChildType.BACKGROUND, cssClass(CLASS_POSITION_ABSOLUTE, CLASS_Z_ZERO, box.background()));
        if (box.border() != null && !box.border().thickness().isZero())
            widgets.put(ChildType.BORDER_FILL, box.border().fill());

        return makePeers(widgets, peers -> {
            update(peers.get(ChildType.CONTENT), peers.get(ChildType.BACKGROUND),
                    peers.get(ChildType.BORDER_FILL));
            return endingWidget();
        });
    }

    private void update(DOMElementHolder childPeer,
                        DOMElementHolder bgPeer,
                        DOMElementHolder borderFillPeer) {
        boolean hasNonColorBackground = false;
        String bgAsCSSImage = bgPeer == null ? null : bgPeer.asCSSImage();
        if (bgPeer != null && bgAsCSSImage == null) // TODO URLImageView-t is kezeljük majd
            hasNonColorBackground = true;

        if (hasNonColorBackground) {
            elem().getClassList().add("p");
            childPeer.element().getClassList().add(CLASS_Z_ZERO);
        } else {
            elem().getClassList().remove(CLASS_POSITION_RELATIVE);
            childPeer.element().getClassList().remove(CLASS_Z_ZERO);
        }

        String cssBackgrounds = "";
        String cssBackgroundOrigins = "";

        if (bgPeer != null && bgAsCSSImage != null) {
            cssBackgrounds = bgAsCSSImage;
            cssBackgroundOrigins = "padding-box";
        }

        BorderSpec border = box.border();
        if (border != null && !border.thickness().isZero()) {
            Insets thickness = border.thickness();
            if (thickness.top().rel() != 0 ||
                    thickness.right().rel() != 0 ||
                    thickness.bottom().rel() != 0 ||
                    thickness.left().rel() != 0)
                throw new RuntimeException("TODO border with relative thickness: " + border);

            String borderAsColor = borderFillPeer.asCSSColor();
            if (borderAsColor != null) {
                elem().getStyle().setProperty("border", "solid " + borderAsColor);
                elem().getStyle().removeProperty("padding");
            } else {
                String borderAsImage = borderFillPeer.asCSSImage();
                if (borderAsImage == null)
                    throw new RuntimeException("TODO border with not a color/gradient/image fill: " + border);

                // feltesszük, hogy van nem áttetsző background (akár itt, akár valamelyik childben).
                // SVG-vel próbáltam trükközni, de azzal se lehet:
                // https://stackoverflow.com/questions/47911938/is-it-possible-to-use-svg-foreignobject-as-a-mask
                // Ha mégis kell, dinamikusan kell átméretezgetni egy svg-t.
                // De valszeg inkább át kéne állni canvas2d-re (vagy teljesen svg-re).

                elem().getStyle().setProperty("border", "solid transparent");
                if (!cssBackgrounds.isEmpty()) {
                    cssBackgrounds += ", ";
                    cssBackgroundOrigins += ", ";
                }
                cssBackgrounds += borderAsImage;
                cssBackgroundOrigins += "border-box";
            }
            elem().getStyle().setProperty("border-width",
                    lengthToCSS(thickness.top()) + " " +
                            lengthToCSS(thickness.right()) + " " +
                            lengthToCSS(thickness.bottom()) + " " +
                            lengthToCSS(thickness.left()));
        } else {
            elem().getStyle().removeProperty("border");
            elem().getStyle().removeProperty("border-width");
            elem().getStyle().removeProperty("padding");
        }

        if (cssBackgrounds.isEmpty())
            elem().getStyle().removeProperty("background");
        else {
            elem().getStyle().setProperty("background", cssBackgrounds.toString());
            elem().getStyle().setProperty("background-origin", cssBackgroundOrigins.toString());
        }

        Length cornerRadius = box.cornerRadius();
        if (cornerRadius.rel() != 0)
            throw new RuntimeException("TODO cornerRadius with non-zero relative part: " + cornerRadius);
        if (cornerRadius.isZero()) {
            elem().getStyle().removeProperty("border-radius");
            elem().getClassList().remove(CLASS_OVERFLOW_HIDDEN);
        } else {
            elem().getStyle().setProperty("border-radius", lengthToCSS(cornerRadius));
            elem().getClassList().add(CLASS_OVERFLOW_HIDDEN);
        }

        BoxShadow shadow = box.boxShadow();
        if (shadow != null) {
            if (shadow.xOffset().rel() != 0 || shadow.yOffset().rel() != 0 ||
                    shadow.blur().rel() != 0 || shadow.spread().rel() != 0)
                throw new RuntimeException("TODO box shadow with relative size: " + shadow);
            elem().getStyle().setProperty("box-shadow",
                    lengthToCSS(shadow.xOffset()) + " " +
                            lengthToCSS(shadow.yOffset()) + " " +
                            lengthToCSS(shadow.blur()) + " " +
                            lengthToCSS(shadow.spread()) + " " +
                            colorToCSS(shadow.color()));
        } else
            elem().getStyle().removeProperty("box-shadow");

        Length fixedWidth = box.fixedSize() == null ? null : box.fixedSize().width();
        Length fixedHeight = box.fixedSize() == null ? null : box.fixedSize().height();
        Length minWidth = fixedWidth != null ? fixedWidth : box.minSize() == null ? null : box.minSize().width();
        Length minHeight = fixedHeight != null ? fixedHeight : box.minSize() == null ? null : box.minSize().height();

        if (fixedWidth != null)
            // TODO bekavarhat display:block
            elem().getStyle().setProperty("max-width", lengthToCSS(fixedWidth));
        else
            elem().getStyle().removeProperty("max-width");

        if (fixedHeight != null)
            elem().getStyle().setProperty("max-height", lengthToCSS(fixedHeight));
        else
            elem().getStyle().removeProperty("max-height");

        if (minWidth != null)
            elem().getStyle().setProperty("min-width", lengthToCSS(minWidth));
        else
            elem().getStyle().removeProperty("min-width");

        if (minHeight != null)
            elem().getStyle().setProperty("min-height", lengthToCSS(minHeight));
        else
            elem().getStyle().removeProperty("min-height");

        if (hasNonColorBackground) {
            if (elem().getChildren().getLength() != 2 ||
                    bgPeer.element() != elem().getChildren().item(0) ||
                    childPeer.element() != elem().getChildren().item(1)) {
                elem().setInnerHTML("");
                DOMLayoutPeerBase.removeAllChildLayoutProperties(bgPeer.element());
                elem().appendChild(bgPeer.element());
                DOMLayoutPeerBase.removeAllChildLayoutProperties(childPeer.element());
                elem().appendChild(childPeer.element());
                // TODO ha csak a bg változik, a childPeert nem kéne cserélni (illetve fordítva)
            }
        } else {
            if (elem().getChildren().getLength() != 1 ||
                    childPeer.element().getParentNode() != elem()) {
                elem().setInnerHTML("");
                DOMLayoutPeerBase.removeAllChildLayoutProperties(childPeer.element());
                elem().appendChild(childPeer.element());
            }
        }

        if (childPeer.element().getNodeName().equals("IMG"))
            childPeer.element().getClassList().add(CLASS_IMG_IN_WRAPPER_ELEMENT);
        else
            childPeer.element().getClassList().remove(CLASS_IMG_IN_WRAPPER_ELEMENT);

        if (borderFillPeer != null || bgPeer != null || box.boxShadow() != null || hasPointerListener())
            elem().getClassList().remove(DOMLayoutPeerBase.CLASS_POINTER_TRANSPARENT_CONTAINER);
        else
            elem().getClassList().add(DOMLayoutPeerBase.CLASS_POINTER_TRANSPARENT_CONTAINER);
    }
}
