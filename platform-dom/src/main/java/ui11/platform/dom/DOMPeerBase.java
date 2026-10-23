package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.Expose;
import ui11.ExposeRequest;
import ui11.Widget;
import ui11.color.Color;
import ui11.color.RGBColor;
import ui11.geom.Length;
import ui11.graphics.Surface;
import ui11.graphics.VisualContentRequest;
import ui11.layout.Insets;
import ui11.platform.dom.DOMWidgetWrapper.InheritedTextStyle;
import ui11.platform.dom.DOMWidgetWrapper.ProxySurface;
import ui11.platform.dom.peers.DOMCoverPeer;
import ui11.text.TextStyle;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;

public abstract class DOMPeerBase<H extends HTMLElement> extends Widget {

    // TODO ha megváltozik a DOMEnv (pl. másik ablakba kerülünk), akkor mi legyen?
    //      kell ilyenkor új elementet gyártani?

    @Inject private DOMEnvironment env;
    @Inject private CumulatingPropList cumulativePropList;
    @Inject private InheritedTextStyle lastAppliedTextStyle;
    @Inject private TextStyle textStyle;
    @Inject private Surface inheritedSurface;
    @Inject private DOMPeerCreationRequest inheritedPeerCreationRequest;

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
        return new ReqWrapperForSingle(widget, f);
    }

    protected Widget makePeers(List<? extends Widget> widgets,
                               Function<List<DOMElementHolder>, Widget> f) {
        return new ReqWrapperForList(widgets, Set.of(),
            (children, a) -> f.apply(children));
    }

    protected final Widget makePeers(List<? extends Widget> widgets,
                                     Set<ExposeRequest<?>> additionalRequests,
                                     BiFunction<List<DOMElementHolder>,
                                         Map<ExposeRequest<?>, ? extends List<?>>, Widget> f) {
        return new ReqWrapperForList(widgets, additionalRequests, f);
    }

    protected <K> Widget makePeers(Map<K, ? extends Widget> widgets,
                                   Function<Map<K, ? extends DOMElementHolder>, Widget> f) {
        return new ReqWrapperForMap<>(widgets, f);
    }

    protected Widget makePeer_sameSurface(Widget widget, Function<DOMElementHolder, Widget> f) {
        // TODO ez most ugyanaz mint a sima makePeer
        return new ReqWrapperForSingle(widget, f);
    }

    protected Widget wrapResult(DOMElementHolder h) {
        return new Expose<>(inheritedPeerCreationRequest, h);
    }

    protected final DOMEnvironment env() {
        return env;
    }

    @SuppressWarnings("unchecked")
    protected final H elem() {
        return (H) elementHolder.element;
    }

    // TODO ehelyett azt kéne, hogy CSSBackgroundImagePeerCreationRequestet is exposeoljunk.
    //      mert most ez így pl. Cover-ben nem működik.
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

    public static final class DOMPeerCreationRequest extends VisualContentRequest<DOMElementHolder> {

        public DOMPeerCreationRequest(@NonNull Surface surface) {
            super(DOMElementHolder.class, surface);
        }
    }

    public static final class CSSBackgroundImagePeerCreationRequest extends VisualContentRequest<DOMCoverPeer.CSSBackgroundImage> {

        public CSSBackgroundImagePeerCreationRequest(Surface surface) {
            super(DOMCoverPeer.CSSBackgroundImage.class, surface);
        }
    }

    // TODO ezeket szüntessük meg
    private static class ReqWrapperForSingle extends Widget {

        private final Widget widget;
        private final Function<DOMElementHolder, Widget> then;

        @Remember private ProxySurface proxySurface;

        public ReqWrapperForSingle(Widget widget, Function<DOMElementHolder, Widget> then) {
            this.widget = Objects.requireNonNull(widget);
            this.then = Objects.requireNonNull(then);
        }

        @Override
        protected void initState() {
            proxySurface = new ProxySurface();
        }

        @Override
        protected Widget build() {
            return ExposeRequest.requestSingle(new DOMWidgetWrapper(widget),
                new DOMPeerCreationRequest(proxySurface), then);
        }
    }

    private static class ReqWrapperForList extends Widget {

        private final List<Widget> widgets;
        private final BiFunction<List<DOMElementHolder>, Map<ExposeRequest<?>, ? extends List<?>>, Widget> then;
        private final Set<ExposeRequest<?>> additionalRequests;

        @Remember private List<ProxySurface> proxySurfaces;

        public ReqWrapperForList(List<? extends Widget> widgets,
                                 Set<ExposeRequest<?>> additionalRequests,
                                 BiFunction<List<DOMElementHolder>, Map<ExposeRequest<?>, ? extends List<?>>, Widget> then) {
            this.widgets = List.copyOf(widgets);
            this.then = Objects.requireNonNull(then);
            this.additionalRequests = Set.copyOf(additionalRequests);
        }

        @Override
        protected void initState() {
            proxySurfaces = new ArrayList<>();
        }

        @Override
        protected Widget build() {
            Map<Integer, Widget> widgetMap = new TreeMap<>();
            Map<Integer, Set<ExposeRequest<?>>> requestsMap = new TreeMap<>();
            List<DOMPeerCreationRequest> elementsRequests = new ArrayList<>();
            for (int i = 0; i < widgets.size(); i++) {
                widgetMap.put(i, new DOMWidgetWrapper(widgets.get(i)));
                Set<ExposeRequest<?>> requests = new HashSet<>(additionalRequests);
                ProxySurface proxySurface;
                if (proxySurfaces.size() == i)
                    proxySurfaces.add(proxySurface = new ProxySurface());
                else
                    proxySurface = proxySurfaces.get(i);
                DOMPeerCreationRequest req = new DOMPeerCreationRequest(proxySurface);
                elementsRequests.add(req);
                requests.add(req);
                requestsMap.put(i, requests);
            }
            if (widgets.size() < proxySurfaces.size())
                proxySurfaces.subList(widgets.size(), proxySurfaces.size()).clear();
            return ExposeRequest.requestMultiple(
                widgetMap,
                requestsMap,
                resultMap -> {
                    List<DOMElementHolder> childElements = new ArrayList<>();
                    for (int i = 0; i < widgets.size(); i++)
                        childElements.add((DOMElementHolder) resultMap.get(elementsRequests.get(i)).get(i));

                    Map<ExposeRequest<?>, List<?>> additionalResponses = resultMap.entrySet().stream().
                        filter(e -> additionalRequests.contains(e.getKey())).
                        collect(toMap(
                            Map.Entry::getKey,
                            e -> IntStream.range(0, widgets.size()).
                                mapToObj((int key) -> {
                                    Object val = e.getValue().get(key);
                                    if (val == null)
                                        throw new RuntimeException("no value for #" + key + ". Available " + resultMap);
                                    return val;
                                }).toList()
                        ));

                    return then.apply(childElements, additionalResponses);
                }
            );
        }
    }

    private static class ReqWrapperForMap<K> extends Widget {

        private final Map<K, Widget> widgets;
        private final Function<Map<K, ? extends DOMElementHolder>, Widget> then;

        @Remember private Map<K, ProxySurface> proxySurfaces;

        public ReqWrapperForMap(Map<K, ? extends Widget> widgets,
                                Function<Map<K, ? extends DOMElementHolder>, Widget> then) {
            this.widgets = Map.copyOf(widgets);
            this.then = Objects.requireNonNull(then);
        }

        @Override
        protected void initState() {
            proxySurfaces = new HashMap<>();
        }

        @Override
        protected Widget build() {
            Map<K, Widget> wrappedWidgets = widgets.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                e -> new DOMWidgetWrapper(e.getValue())
            ));
            Map<K, ExposeRequest<?>> reqs = new HashMap<>();
            Map<K, Set<ExposeRequest<?>>> reqSets = new HashMap<>();
            for (K k : widgets.keySet()) {
                ProxySurface proxySurface = proxySurfaces.computeIfAbsent(k, __ -> new ProxySurface());
                DOMPeerCreationRequest req = new DOMPeerCreationRequest(proxySurface);
                reqs.put(k, req);
                reqSets.put(k, Set.of(req));
            }
            proxySurfaces.keySet().retainAll(widgets.keySet());

            return ExposeRequest.requestMultiple(wrappedWidgets, reqSets, results -> {
                Map<K, DOMElementHolder> children = new HashMap<>();
                for (K k : widgets.keySet())
                    children.put(k, (DOMElementHolder) Objects.requireNonNull(results.get(reqs.get(k)).get(k)));
                return then.apply(children);
            });
        }
    }
}
