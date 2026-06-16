package ui11.platform.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Element;
import ui11.Widget;
import ui11.platform.dom.peers.DOMBoxPeer;

import java.util.List;
import java.util.Set;

/**
 * doBuild (or an inner widget) should call {@link #updateChildren(List)} and return its result
 */
public abstract class DOMLayoutPeerBase extends DOMPeerBase<HTMLElement> {

    private static final Logger logger = LoggerFactory.getLogger(DOMLayoutPeerBase.class);

    public static final String CLASS_POINTER_TRANSPARENT_CONTAINER = "pE";

    // TODO ez legyen inkább függvény, mert így beleszámít equals/hashCodeba
    private final boolean usesFlexGrowOnChildren;
    private final boolean usesGridAreaOnChildren;

    public DOMLayoutPeerBase(boolean usesFlexGrowOnChildren, boolean usesGridAreaOnChildren) {
        this.usesFlexGrowOnChildren = usesFlexGrowOnChildren;
        this.usesGridAreaOnChildren = usesGridAreaOnChildren;
    }

    @Override
    protected Widget endingWidget() {
        if (mouseTransparent() && !hasPointerListener())
            elem().getClassList().add(CLASS_POINTER_TRANSPARENT_CONTAINER);
        else
            elem().getClassList().remove(CLASS_POINTER_TRANSPARENT_CONTAINER);

        return super.endingWidget();
    }

    protected Widget updateToSingleChild(Widget singleChild) {
        return makePeer(singleChild,
                h -> updateChildren(List.of(h.element())));
    }

    protected Widget updateChildren(List<? extends HTMLElement> newChildren) {
        if (childrenChanged(newChildren)) {
            int i = 0;
            // TODO kéne egy rendes diff algoritmust nézni, mert ez így fölöslegesen sok DOM mutationt végez
            //      és emiatt pl. újraindulhatnak SVG animációk meg fene tudja mik történhetnek
            Set<Element> neededElements = Set.copyOf(newChildren);
            if (neededElements.size() != newChildren.size())
                throw new RuntimeException("duplicate elements in " + newChildren +
                        ", probably somebody used withKey twice with same key");
            for (HTMLElement p : newChildren) {
                removeUnusedChildLayoutProperties(p);
                if (elem().getChildren().getLength() == i) {
                    //System.out.println(this + ": append " + p.htmlElement);
                    elem().appendChild(p);
                } else if (elem().getChildren().item(i) != p) {
                    //System.out.println(this + ": swap " + i + " with " + p.htmlElement);
                    elem().insertBefore(p, elem().getChildren().item(i));
                    Element elemInPlace = elem().getChildren().item(i + 1);
                    if (!neededElements.contains(elemInPlace))
                        elem().removeChild(elemInPlace);
                }
                i++;
            }
            while (elem().getChildren().getLength() > i) {
                //System.out.println(this + ": remove excess element");
                elem().removeChild(elem().getChildren().item(elem().getChildren().getLength() - 1));
            }
        }
        return endingWidget();
    }

    private void removeUnusedChildLayoutProperties(HTMLElement htmlElement) {
        if (!usesFlexGrowOnChildren)
            htmlElement.getStyle().removeProperty("flex-grow");
        if (!usesGridAreaOnChildren)
            htmlElement.getStyle().removeProperty("grid-area");
        htmlElement.getStyle().removeProperty(DOMBoxPeer.CLASS_IMG_IN_WRAPPER_ELEMENT);
    }

    public static void removeAllChildLayoutProperties(HTMLElement htmlElement) {
        htmlElement.getStyle().removeProperty("flex-grow");
        htmlElement.getStyle().removeProperty("grid-area");
        htmlElement.getClassList().remove(DOMBoxPeer.CLASS_IMG_IN_WRAPPER_ELEMENT);
    }

    // pl. Scrollable nem mouse transparent, mert a scrollbart kell tudni egérmozdulattal irányítani
    protected boolean mouseTransparent() {
        return true;
    }

    private boolean childrenChanged(List<? extends HTMLElement> newChildren) {
        if (newChildren.size() != elem().getChildren().getLength())
            return true;

        for (int i = 0; i < newChildren.size(); i++) {
            if (elem().getChildren().item(i) != newChildren.get(i))
                return true;
        }
        return false;
    }
}
