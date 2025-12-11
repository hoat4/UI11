package ui11.platform.dom;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.geom.Length;
import ui11.graphics.Surface;
import ui11.graphics.fill.Color;
import ui11.layout.Insets;
import ui11.observable.Observable;
import ui11.platform.dom.DOMWidgetWrapper.InheritedTextStyle;
import ui11.platform.dom.DOMWidgetWrapper.ProxySurface;
import ui11.provide.UpValueWrapper;
import ui11.text.TextStyle;

public abstract class DOMPeerBase<H extends HTMLElement> extends Widget {

    // TODO ennek nem kéne observable-nek lennie
    @Inject private Observable<DOMEnvironment> env;
    @Inject private Observable<CumulatingPropList> cumulativePropList;
    @Inject private Observable<InheritedTextStyle> lastAppliedTextStyle;
    @Inject private Observable<TextStyle> textStyle;
    @Inject private Observable<Surface> inheritedSurface;

    @State private DOMSurface elementHolder;

    @Override
    protected void initState() {
    }

    protected H fixedElement() {
        return null;
    }

    /**
     * Ha nem div kell, akkor ezt felül kell írni. Ha {@linkplain #fixedElement()} nem null, akkor ez nincs
     * figyelembe véve.
     */
    protected String elementName() {
        return "div";
    }

    protected abstract void initElement();

    @Override
    protected final Widget build() {
        H fixedElement = fixedElement();
        if (elementHolder == null ||
                (fixedElement != null ? fixedElement != elementHolder.element :
                !elem().getNodeName().equalsIgnoreCase(elementName()))) {
            elementHolder = new DOMSurface(env.get(),
                    fixedElement == null ? env.get().document.createElement(elementName()) : fixedElement);
            initElement();
        }

        // TODO CSSClassTag.wrapet is kéne tudni támogatni

        ProxySurface proxySurface = (ProxySurface) inheritedSurface.get();
        CumulatingPropList cumulativePropList = this.cumulativePropList.get();
        TextStyle lastAppliedTextStyle = this.lastAppliedTextStyle.get().ts();
        TextStyle textStyle = this.textStyle.get().diffTo(lastAppliedTextStyle);

        elementHolder.update(proxySurface, cumulativePropList, textStyle);

        update();

        DOMElementHolder result = new DOMElementHolder(elementHolder.element, asCSSColor(), asCSSImage());
        return wrapResult(result);
    }

    protected Widget wrapResult(DOMElementHolder h) {
        return new UpValueWrapper(h);
    }

    protected final DOMEnvironment env() {
        return env.get();
    }

    @SuppressWarnings("unchecked")
    protected final H elem() {
        return (H) elementHolder.element;
    }

    protected abstract void update();

    protected String asCSSColor() {
        return null;
    }

    protected String asCSSImage() {
        String color = asCSSColor();
        if (color == null)
            return null;
        else
            return "linear-gradient(" + color + ", " + color + ")";
    }

    public static String colorToCSS(Color color) {
        int alpha = color.alphaAsInt8();
        String r = Integer.toHexString(color.redAsInt8()),
                g = Integer.toHexString(color.greenAsInt8()),
                b = Integer.toHexString(color.blueAsInt8()),
                a = alpha == 255 ? "" : Integer.toHexString(alpha);
        if (r.length() == 1)
            r = "0" + r;
        if (g.length() == 1)
            g = "0" + g;
        if (b.length() == 1)
            b = "0" + b;
        if (a.length() == 1)
            a = "0" + a;
        return "#" + r + g + b + a;
    }

    public static String lengthToCSS(Length len) {
        // TODO double-knek nem kéne olyan sok számjegy

        if (len.em() == 0 && len.px() == 0 && len.rel() == 0) {
            return "0";
        }
        if (len.em() == 0 && len.px() == 0) {
            return len.rel() * 100 + "%";
        }
        if (len.em() == 0 && len.rel() == 0) {
            return len.px() + "px";
        }
        if (len.px() == 0 && len.rel() == 0) {
            return len.em() + "em";
        }

        String s = "calc(";
        if (len.em() != 0) {
            s += len.em() + "em + ";
        }
        if (len.px() != 0) {
            s += len.px() + "px";
        }
        if (len.rel() != 0) {
            if (len.px() != 0)
                s += " + ";
            s += len.rel() * 100 + "%";
        }
        s += ")";
        return s;
    }

    public static String insetsToCSS(Insets insets) {
        return lengthToCSS(insets.top()) + " " +
                lengthToCSS(insets.right()) + " " +
                lengthToCSS(insets.bottom()) + " " +
                lengthToCSS(insets.left()) + " ";
    }

    // ezt lehet hogy jobb lenne eltárolni egy Observable<Boolean>-ben.
    // TODO specifikálni hogy mi ez, és leírni, hogy mire használjuk. amikor beraktam hogy PointerRegiont is nézze,
    //      akkor például a Practice gomb hover effektje elromlott.
    public boolean hasPointerListener() {
        return !cumulativePropList.get().onClick().isEmpty();
    }
}
