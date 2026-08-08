package ui11.platform.dom;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.ParentData;
import ui11.PeerRequestor;
import ui11.Widget;
import ui11.color.RGBColor;
import ui11.geom.Length;
import ui11.graphics.Surface;
import ui11.color.Color;
import ui11.layout.Insets;
import ui11.platform.dom.DOMWidgetWrapper.InheritedTextStyle;
import ui11.platform.dom.DOMWidgetWrapper.ProxySurface;
import ui11.platform.dom.peers.DOMCoverPeer;
import ui11.text.TextStyle;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

public abstract class DOMPeerBase<H extends HTMLElement> extends Widget {

    // TODO ha megváltozik a DOMEnv (pl. másik ablakba kerülünk), akkor mi legyen?
    //      kell ilyenkor új elementet gyártani?

    @Inject private DOMEnvironment env;
    @Inject private CumulatingPropList cumulativePropList;
    @Inject private InheritedTextStyle lastAppliedTextStyle;
    @Inject private TextStyle textStyle;
    @Inject private Surface inheritedSurface;

    @Remember private DOMSurface elementHolder;

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
            elementHolder = new DOMSurface(env,
                    fixedElement == null ? env.document.createElement(elementName()) : fixedElement);
            initElement();
        }

        // TODO CSSClassTag.wrapet is kéne tudni támogatni

        ProxySurface proxySurface = (ProxySurface) inheritedSurface;
        CumulatingPropList cumulativePropList = this.cumulativePropList;
        TextStyle lastAppliedTextStyle = this.lastAppliedTextStyle.ts();
        TextStyle textStyle = this.textStyle.diffTo(lastAppliedTextStyle);

        elementHolder.update(proxySurface, cumulativePropList, textStyle);

        return doBuild();
    }

    protected abstract Widget doBuild();

    protected Widget endingWidget() {
        DOMElementHolder result = new DOMElementHolder(elementHolder.element, asCSSColor(), asCSSImage());
        return wrapResult(result);
    }

    protected Widget makePeer(Widget widget, Function<DOMElementHolder, Widget> f) {
        return PeerRequestor.ofSingle(new DOMWidgetWrapper(widget), new DOMPeerCreationRequest(),
                result -> f.apply(result.peer()));
    }

    @SuppressWarnings("SimplifyStreamApiCallChains")
    protected Widget makePeers(List<? extends Widget> widgets,
                               Function<List<DOMElementHolder>, Widget> f) {
        return PeerRequestor.ofMultipleWidgets(
                widgets.stream().map(DOMWidgetWrapper::new).toList(),
                new DOMPeerCreationRequest(),
                results ->
                f.apply(results.stream().map(PeerRequestor.Result::peer).
                        collect(Collectors.toUnmodifiableList()))
        );
    }

    protected final Widget makePeers(List<? extends Widget> widgets,
                                     Set<PeerRequestor.Request<?>> additionalRequests,
                                     BiFunction<List<DOMElementHolder>,
                                             Map<PeerRequestor.Request<?>, ? extends List<?>>, Widget> f) {
        Set<PeerRequestor.Request<?>> requests = new HashSet<>(additionalRequests);
        requests.add(new DOMPeerCreationRequest());
        return PeerRequestor.ofMultipleRequests(
                widgets.stream().map(DOMWidgetWrapper::new).toList(),
                requests,
                f
        );
    }

    protected <K> Widget makePeers(Map<K, ? extends Widget> widgets,
                                   Function<Map<K, ? extends DOMElementHolder>, Widget> f) {
        widgets = widgets.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                e -> new DOMWidgetWrapper(e.getValue()))
        );
        return PeerRequestor.ofMultipleWidgets(widgets, new DOMPeerCreationRequest(), results ->
                f.apply(results.entrySet().stream().
                        collect(toUnmodifiableMap(
                                Map.Entry::getKey,
                                e -> e.getValue().peer())
                        )));
    }

    protected Widget makePeer_sameSurface(Widget widget, Function<DOMElementHolder, Widget> f) {
        // TODO ez most ugyanaz mint a sima makePeer
        return PeerRequestor.ofSingle(new DOMWidgetWrapper(widget), new DOMPeerCreationRequest(),
                result -> f.apply(result.peer()));
    }

    protected Widget wrapResult(DOMElementHolder h) {
        return h;
    }

    protected final DOMEnvironment env() {
        return env;
    }

    @SuppressWarnings("unchecked")
    protected final H elem() {
        return (H) elementHolder.element;
    }

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
        RGBColor srgb = color.toSRGB(); // TODO
        int alpha = srgb.alphaAsInt8();
        String r = Integer.toHexString(srgb.redAsInt8()),
                g = Integer.toHexString(srgb.greenAsInt8()),
                b = Integer.toHexString(srgb.blueAsInt8()),
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
        return !cumulativePropList.onClick().isEmpty();
    }

    public static final class DOMPeerCreationRequest extends PeerRequestor.Request<DOMElementHolder> {

        public DOMPeerCreationRequest() {
            super(DOMElementHolder.class);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof DOMPeerCreationRequest;
        }

        @Override
        public int hashCode() {
            return 34411343;
        }
    }

    public static final class CSSBackgroundImagePeerCreationRequest extends PeerRequestor.Request<DOMCoverPeer.CSSBackgroundImage> {

        public CSSBackgroundImagePeerCreationRequest() {
            super(DOMCoverPeer.CSSBackgroundImage.class);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof CSSBackgroundImagePeerCreationRequest;
        }

        @Override
        public int hashCode() {
            return 340404043;
        }
    }
}
