package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * A widget that requests some information about some child widgets, then invokes a callback with the results.
 * <p>
 * The requests can be accessed in the following ways:
 * <ul>
 *     <li>By implementing the {@link WidgetResolver#tryResolveRequestSpecific(SubstitutedWidget, Request)} method,
 *     which will be called for each request
 *     <li>Declaring a {@link ui11.Widget.Inject @Inject} field in a widget whose type is the array type of the request
 *     type. The array's value will be the current peer requests that should be fulfilled. The array won't contain two
 *     {@linkplain Object#equals(Object) same} element twice.
 * </ul>
 * A widget can provide response to a request by returning the value of
 * {@linkplain Request#createResponse(Object) Request.createResponse} or
 * {@linkplain Request#createResponse(Object, Widget)}.
 */
public abstract sealed class PeerRequestor extends Widget {

    final Set<Class<? extends ParentData>> interestedParentDataTypes;
    final boolean clearParentData;

    private PeerRequestor(Set<Class<? extends ParentData>> interestedParentDataTypes, boolean clearParentData) {
        this.interestedParentDataTypes = interestedParentDataTypes;
        for (Class<?> c : interestedParentDataTypes)
            if (!ParentData.class.isAssignableFrom(c))
                throw new IllegalArgumentException("not a " + ParentData.class.getSimpleName() + " subtype: " +
                        c.getName());

        this.clearParentData = clearParentData;
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("should not reach here (PR.b)");
    }

    /**
     * @return ha null, ha nincs child. üres tömb nem lehet.
     */
    abstract WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren);

    /**
     * @throws NullPointerException     ha parentDataTypes vagy egy eleme {@code null}
     * @throws ClassCastException       ha parentDataTypes legalább egy eleme nem assignable to {@link ParentData}
     * @throws IllegalArgumentException ha parentDataTypes legalább egy eleme már hozzá lett adva az
     *                                  interested ParentData type-ok listájához
     */
    // ez a függvény nem használható a Collectoros API-val együtt. ezzel csináljunk valamit vagy nem baj?
    @SuppressWarnings("DataFlowIssue") // IDE szerint redundáns asSubclass
    @SafeVarargs
    public final @NonNull PeerRequestor withInterestedParentDataType(Class<? extends ParentData>... parentDataTypes) {
        if (parentDataTypes.length == 0)
            return this;

        Set<Class<? extends ParentData>> set = new HashSet<>(interestedParentDataTypes);
        for (Class<? extends ParentData> parentDataType : parentDataTypes) {
            Objects.requireNonNull(parentDataType);
            parentDataType = parentDataType.asSubclass(ParentData.class);
            if (!set.add(parentDataType))
                throw new IllegalArgumentException("Parent data type already added: " + parentDataType.getName());
        }
        return withInterestedParentDataType(Set.copyOf(set));
    }

    abstract PeerRequestor withInterestedParentDataType(Set<Class<? extends ParentData>> parentDataTypes);

    abstract PeerRequestor withClearParentData(boolean clearParentData);

    @NullMarked
    public static <P> PeerRequestor ofSingle(Widget widget,
                                             Request<P> request,
                                             Function<Result<P>, Widget> then) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(request);
        Objects.requireNonNull(then);
        return new CreatePeerForSingle<>(widget, request, then, Set.of(), true);
    }

    // TODO REQ extends Request<P> nem lehet, mert akkor egy Set<Request<?>>-vel nem lehet meghívni ezt a függvényt
    @NullMarked
    public static <REQ extends Request<?>, RES extends Result<?>> PeerRequestor ofSingleWidget(Widget widget,
                                                                                               Set<REQ> requests,
                                                                                               Function<Map<REQ, RES>, Widget> then) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(requests);
        Objects.requireNonNull(then);

        enum Single {SINGLE}
        return PeerRequestor.ofMultiple(
                Map.of(Single.SINGLE, widget),
                Map.of(Single.SINGLE, Set.copyOf(requests)),
                (Map<Request<?>, Map<Single, Result<?>>> results) -> {
                    Map<Request<?>, Result<?>> results2 = results.entrySet().stream().collect(toUnmodifiableMap(
                            Map.Entry::getKey, (Map.Entry<Request<?>, Map<Single, Result<?>>> entry) -> {
                                Map<Single, Result<?>> m = entry.getValue();
                                Result<?> result = m.get(Single.SINGLE);
                                assert result != null;
                                return result;
                            }));
                    @SuppressWarnings("unchecked") Map<REQ, RES> castedMap = (Map<REQ, RES>) results2;
                    return then.apply(castedMap);
                });
    }

    // azért nem ofMultiple, mert az arra utalna, hogy több req is van, nem csak több widget
    @NullMarked
    public static <P> PeerRequestor ofMultipleWidgets(List<? extends Widget> widgets,
                                                      Request<P> request,
                                                      Function<List<Result<P>>, Widget> then) {
        widgets = List.copyOf(widgets);
        return new CreatePeersForList<>(widgets, Collections.nCopies(widgets.size(), request), then, Set.of(), true);
    }


    @NullMarked
    public static <K, P> PeerRequestor ofMultipleWidgets(Map<K, ? extends Widget> widgets,
                                                         Request<P> request,
                                                         Function<Map<K, Result<P>>, Widget> then) {
        // most ez a Map.copyOf NPE-t akkor is, ha K-k között van egy null.
        // ha mégis kell null K, akkor kézzel kell ellenőrizni
        // (de akkor ofMultiple-t is módosítsuk eszerint)
        // TODO kéne írni OpenJDK-nak, hogy Collectors.toMap null value-knál exceptiont dob
        widgets = Map.copyOf(widgets);
        Objects.requireNonNull(request);
        Objects.requireNonNull(then);

        Map<K, ? extends Set<Request<P>>> requests = widgets.keySet().stream().
                collect(toMap(k -> k, k -> Set.of(request)));
        return new PeerRequestor.CreatePeersForMap<>(
                widgets,
                requests,
                results -> {
                    Map<K, Result<P>> map = results.get(request);
                    assert map != null;
                    return then.apply(map);
                },
                Set.of(),
                true
        );
    }

    @NullMarked
    public static <P> Widget ofMultiple(
            List<? extends Widget> widgets,
            List<? extends Request<P>> requests,
            Function<List<Result<P>>, Widget> then) {
        widgets = List.copyOf(widgets);
        requests = List.copyOf(requests);
        Objects.requireNonNull(then);
        if (widgets.size() != requests.size())
            throw new IllegalArgumentException();

        return new PeerRequestor.CreatePeersForList<>(widgets, requests, then, Set.of(), true);
    }

    @NullMarked
    public static Widget ofMultipleRequests(
            List<? extends Widget> widgets,
            Set<Request<?>> requests,
            Function<Map<Request<?>, ? extends List<?>>, Widget> then) {
        List<? extends Widget> widgets2 = List.copyOf(widgets);
        Set<Request<?>> requests2 = Set.copyOf(requests);
        Objects.requireNonNull(then);

        // TODO CreatePeersForList nem tud több requestet, de ez a mapes izé meg lassú
        Map<Integer, Widget> widgetsMap = IntStream.range(0, widgets2.size()).
                boxed().collect(toUnmodifiableMap(i -> i, widgets2::get));
        Map<Integer, Set<Request<?>>> requestsMap = IntStream.range(0, widgets2.size()).
                boxed().collect(toUnmodifiableMap(i -> i, i -> requests2));
        return ofMultiple(widgetsMap, requestsMap, resultMap -> {
            assert resultMap.keySet().equals(requests2);
            Map<Request<?>, List<Object>> lists = new HashMap<>();
            resultMap.forEach((req, resultsForReq) -> {
                List<Object> list = new ArrayList<>();
                for (int i = 0; i < widgets2.size(); i++) {
                    list.add(resultsForReq.get(i).peer());
                }
                lists.put(req, list);
            });
            return then.apply(lists);
        });
    }

    @NullMarked
    public static <K> PeerRequestor ofMultiple(Map<K, ? extends Widget> widgets,
                                               Map<K, Set<Request<?>>> requests,
                                               Function<Map<Request<?>, Map<K, Result<?>>>, Widget> then) {
        // most ez a Map.copyOf NPE-t akkor is, ha K-k között van egy null.
        // ha mégis kell null K, akkor kézzel kell ellenőrizni
        // (de akkor ofMultiple-t is módosítsuk eszerint)
        // TODO kéne írni OpenJDK-nak, hogy Collectors.toMap null value-knál exceptiont dob
        widgets = Map.copyOf(widgets);
        requests = requests.entrySet().stream().collect(toUnmodifiableMap(
                Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
        @SuppressWarnings("unchecked") Map<K, Set<Request<Object>>> requests2 = (Map<K, Set<Request<Object>>>) (Map<K, ?>) requests;
        @SuppressWarnings("unchecked") Function<Map<Request<Object>, Map<K, Result<Object>>>, Widget> then2 =
                (Function<Map<Request<Object>, Map<K, Result<Object>>>, Widget>) (Function<?, Widget>) then;
        return new PeerRequestor.CreatePeersForMap<>(widgets, requests2, then2, Set.of(), true);
    }

    // TODO név?
    @NullMarked
    public static <P> Collector<Widget, ?, Widget> collector(
            Request<P> request,
            Function<Stream<Result<P>>, Widget> then) {
        // then-ben azért Stream van List helyett, mert így majd lehetne
        // olyat csinálni, hogy mondjuk ne resolveolja a végén lévő
        // widgeteket feleslegesen, ha azok resultjait úgyse olvassuk ki.
        // mondjuk ez félig értelmetlenné vált most hogy átálltunk iteratív tree refreshgre.
        return Collectors.collectingAndThen(Collectors.toList(),
                list -> {
                    list = List.copyOf(list);
                    return new PeerRequestor.CreatePeersForList<>(
                            list,
                            Collections.nCopies(list.size(), request),
                            l -> then.apply(l.stream()),
                            Set.of(),
                            true
                    );
                });
    }

    /**
     * Describes what information to query about a widget (or multiple widgets).
     * For example, on a layout query, the Request object would contain the size constraints,
     * and the result will be the computed size.
     * <p>
     * If two requests are equal according to {@link Object#equals(Object)}, then they are performed only once.
     */
    public abstract static class Request<P> {

        private final Class<P> peerType;

        protected Request(Class<P> peerType) {
            this.peerType = peerType;
        }

        // TODO van valami haszna, hogy ez publikus?
        public final Class<P> peerType() {
            return peerType;
        }

        /**
         * Ha nem {@code null}, akkor van defaultja, ekkor az első {@linkplain SubstitutedWidget SubstitutedWidgetnél}
         * meg fog állni a keresése. Ha {@code null}, akkor nincs defaultja
         */
        // TODO cache value of this
        protected @Nullable P defaultValue() {
            return null;
        }

        public final Widget createResponse(P peer) {
            Objects.requireNonNull(peer);
            return new ResponseWidget<>(this, peer, null);
        }

        public final Widget createResponse(P peer, @NonNull Widget chainedWidget) {
            Objects.requireNonNull(peer);
            return new ResponseWidget<>(this, peer, chainedWidget);
        }
    }

    static final class ResponseWidget<P> extends Widget {

        private final @NonNull Request<P> request;
        private final @NonNull P peer;
        private final @Nullable Widget chainedWidget;

        @Inject private ResolutionRequestCollection peerCreationRequestCollection;

        // TODO ezt csak akkor kéne lekérdezni és observálni, ha resolutionRequest.requestData.peerType().isInstance(this)
        @Inject(required = false) private ParentDataWidget.ParentDataCollection parentDataCollection;

        public ResponseWidget(@NonNull Request<P> request, @NonNull P peer, @Nullable Widget chainedWidget) {
            this.request = request;
            this.peer = peer;
            this.chainedWidget = chainedWidget;
        }

        @Override
        protected Widget build() {
            Map<Request<?>, ResolutionRequest<?>> remaining = new HashMap<>();
            for (ResolutionRequest<?> resolutionRequest : peerCreationRequestCollection.requests()) {
                if (resolutionRequest.requestData.peerType().isInstance(peer) &&
                        request.equals(resolutionRequest.requestData)) {
                    List<? extends ParentDataWidget> parentDataList =
                            parentDataCollection == null ? List.of() : parentDataCollection.parentDataList;
                    // TODO ha már kapott resultot ebben a refreshben, akkor az újabbakat ignorálnia kéne vagy beraknia?
                    // TODO ha this instanceof ParentDataWidget, akkor értelmetlen hogy setResultUncheckedben
                    //      ellenőrizzük a next widget egyezőségét is
                    resolutionRequest.setResultUnchecked(peer, parentDataList,
                            widgetState().tree.beganRefreshID);
                } else {
                    if (remaining.put(resolutionRequest.requestData, resolutionRequest) != null)
                        // több ResolutionRequest tartozik egy Requesthez
                        throw new RuntimeException("TODO");
                }
            }

            if (remaining.keySet().stream().allMatch(req -> req.defaultValue() != null)) {
                // ugyanaz mint SubstitutedWidget elején
                remaining.forEach((req, resolutionRequest) -> {
                    resolutionRequest.setResultUnchecked(req.defaultValue(), List.of(), widgetState().tree.beganRefreshID);
                });
                return new WidgetTree.ChainEnd();
            }

            if (chainedWidget == null)
                throw new RuntimeException("TODO");

            return PeerRequestor.ofSingleWidget(chainedWidget, remaining.keySet(), respMap -> {
                remaining.forEach((req, resReq) -> {
                    Result<?> result2 = respMap.get(req);
                    assert result2 != null;

                    // TODO lásd fenti kommentek
                    resReq.setResultFrom(result2);
                });
                return new WidgetTree.ChainEnd();
            }).withClearParentData(false).withInterestedParentDataType(ParentData.class);
        }
    }

    /**
     * The resulting peer and parent data (if available) of a peer resolution.
     * Peer is always available (else the resolution fails). Parent data is only available for the
     * {@linkplain PeerRequestor#withInterestedParentDataType(Set) interested types}, and only if they are set via
     * {@linkplain ParentData#attach(ParentData, Widget)}.
     */
    public static final class Result<P> {

        // TODO ez a req miért kell ide? equalst nem befolyásolja? Request nem lenne elég helyette?
        final @NonNull ResolutionRequest<P> req;
        private final @NonNull P peer;
        private final @NonNull Map<Class<? extends ParentData>, ParentData> parentDataMap;

        Result(@NonNull ResolutionRequest<P> req,
               @NonNull P peer,
               @NonNull Map<Class<? extends ParentData>, ParentData> parentDataMap) {
            this.req = req;
            this.peer = peer;
            this.parentDataMap = parentDataMap;
        }

        public P peer() {
            return peer;
        }

        public Map<Class<? extends ParentData>, ParentData> parentDataList() {
            return parentDataMap;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Result<?> that = (Result<?>) o;
            return req.equals(that.req) && peer.equals(that.peer) && parentDataMap.equals(that.parentDataMap);
        }

        @Override
        public int hashCode() {
            int result = req.hashCode();
            result = 31 * result + peer.hashCode();
            result = 31 * result + parentDataMap.hashCode();
            return result;
        }

        // TODO toString most túl sok információt tartalmaz, mert req is benne van, ami többi függvényben nincs

        @Override
        public String toString() {
            return "PeerRequestor.Result{" +
                    "req=" + req +
                    ", peer=" + peer +
                    ", parentDataList=" + parentDataMap +
                    '}';
        }
    }

    static final class CreatePeerForSingle<P> extends PeerRequestor {

        private final Widget widget;
        private final Request<P> request;
        private final Function<Result<P>, Widget> f;

        @Remember private ResolutionRequest<P> req;

        @NullMarked
        public CreatePeerForSingle(Widget widget, Request<P> request, Function<Result<P>, Widget> f,
                                   Set<Class<? extends ParentData>> interestedParentDataTypes,
                                   boolean clearParentData) {
            super(interestedParentDataTypes, clearParentData);
            this.widget = widget;
            this.request = request;
            this.f = f;
        }

        @Override
        PeerRequestor withInterestedParentDataType(Set<Class<? extends ParentData>> parentDataTypes) {
            return new CreatePeerForSingle<>(widget, request, f, parentDataTypes, clearParentData);
        }

        @Override
        PeerRequestor withClearParentData(boolean clearParentData) {
            return new CreatePeerForSingle<>(widget, request, f, interestedParentDataTypes, clearParentData);
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            if (req == null || !Objects.equals(req.requestData, request) ||
                    !Objects.equals(req.widget, widget) ||
                    !Objects.equals(interestedParentDataTypes, req.interestedParentDataTypes))
                req = new ResolutionRequest<>(
                        widgetState, request, widget, interestedParentDataTypes);
            WidgetInstantiation reqW = widgetState.tree.findOrCreateWidgetState(
                    req.widget,
                    widgetState,
                    existingChildren == null ? null : existingChildren[0],
                    Set.of(req),
                    clearParentData
            );
            WidgetInstantiation finisher = widgetState.tree.findOrCreateWidgetState(
                    new SingleRRFinisher<>(req, f),
                    widgetState,
                    existingChildren == null ? null : existingChildren[1],
                    null,
                    false
            );
            req.finisherWidget = finisher.child();
            return new WidgetInstantiation[]{reqW, finisher};
        }

        private static class SingleRRFinisher<P> extends FinisherWidget {

            private final ResolutionRequest<P> req;
            private final Function<Result<P>, Widget> f;

            public SingleRRFinisher(ResolutionRequest<P> req, Function<Result<P>, Widget> f) {
                this.req = req;
                this.f = f;
            }

            @Override
            protected Widget build() {
                return f.apply(req.resultOrFail());
            }

            @Override
            String fToString() {
                return f.getClass().getName();
            }
        }
    }

    static final class CreatePeersForList<P> extends PeerRequestor {

        private final List<? extends Widget> widgets;
        private final List<? extends Request<P>> requests;
        private final Function<? super List<Result<P>>, Widget> f;

        @Remember private ResolutionRequest<P>[] reqs;

        public CreatePeersForList(List<? extends Widget> widgets,
                                  List<? extends Request<P>> requests,
                                  Function<? super List<Result<P>>, Widget> f,
                                  Set<Class<? extends ParentData>> interestedParentDataTypes,
                                  boolean clearParentData) {
            super(interestedParentDataTypes, clearParentData);
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        PeerRequestor withInterestedParentDataType(Set<Class<? extends ParentData>> parentDataTypes) {
            return new CreatePeersForList<>(widgets, requests, f, parentDataTypes, clearParentData);
        }

        @Override
        PeerRequestor withClearParentData(boolean clearParentData) {
            return new CreatePeersForList<>(widgets, requests, f, interestedParentDataTypes, clearParentData);
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            if (reqs == null) {
                @SuppressWarnings("unchecked")
                ResolutionRequest<P>[] reqsArray = new ResolutionRequest[widgets.size()];
                reqs = reqsArray;
            } else if (reqs.length != widgets.size())
                reqs = Arrays.copyOf(reqs, widgets.size());

            WidgetInstantiation[] children = new WidgetInstantiation[widgets.size() + 1];

            WidgetState<?> finisher = (children[widgets.size()] = widgetState.tree.findOrCreateWidgetState(
                    new ListRRFinisher<>(reqs, f),
                    widgetState,
                    existingChildren == null ? null : existingChildren[existingChildren.length - 1],
                    null,
                    false
            )).child();

            for (int i = 0; i < widgets.size(); i++) {
                ResolutionRequest<P> req = reqs[i];
                if (req == null || !Objects.equals(req.requestData, this.requests.get(i)) ||
                        !Objects.equals(req.widget, widgets.get(i)) ||
                        !Objects.equals(interestedParentDataTypes, req.interestedParentDataTypes)) {
                    reqs[i] = req = new ResolutionRequest<>(
                            widgetState,
                            this.requests.get(i), widgets.get(i),
                            interestedParentDataTypes);
                    req.finisherWidget = finisher;
                }
                WidgetInstantiation existingWidgetState =
                        existingChildren != null && existingChildren.length - 1 > i ?
                                existingChildren[i] : null;
                children[i] = widgetState.tree.findOrCreateWidgetState(
                        req.widget,
                        widgetState,
                        existingWidgetState,
                        Set.of(req),
                        clearParentData
                );
            }

            return children;
        }

        private static class ListRRFinisher<P> extends FinisherWidget {

            private final ResolutionRequest<P>[] reqs;
            private final Function<? super List<Result<P>>, Widget> f;

            public ListRRFinisher(ResolutionRequest<P>[] reqs,
                                  Function<? super List<Result<P>>, Widget> f) {
                this.reqs = reqs;
                this.f = f;
            }

            @Override
            protected Widget build() {
                @SuppressWarnings("unchecked")
                Result<P>[] results = new Result[reqs.length];
                for (int i = 0; i < results.length; i++)
                    results[i] = reqs[i].resultOrFail();
                // TODO dokumentálni kéne, hogy f nem null-toleráns Listet kap
                return f.apply(List.of(results));
            }

            @Override
            String fToString() {
                return f.getClass().getName();
            }
        }
    }

    static final class CreatePeersForMap<P, K> extends PeerRequestor {

        private final Map<K, ? extends Widget> widgets;
        private final Map<K, ? extends Set<Request<P>>> requests;
        private final Function<? super Map<Request<P>, Map<K, Result<P>>>, Widget> f;

        @Remember private Key.KeyCache<K> slots;
        @Remember private Map<K, Set<ResolutionRequest<P>>> reqs;

        public CreatePeersForMap(Map<K, ? extends Widget> widgets,
                                 Map<K, ? extends Set<Request<P>>> requests,
                                 Function<? super Map<Request<P>, Map<K, Result<P>>>, Widget> f,
                                 Set<Class<? extends ParentData>> interestedParentDataTypes,
                                 boolean clearParentData) {
            super(interestedParentDataTypes, clearParentData);
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        protected void initState() {
            slots = new Key.KeyCache<>();
        }

        @Override
        PeerRequestor withInterestedParentDataType(Set<Class<? extends ParentData>> parentDataTypes) {
            return new CreatePeersForMap<>(widgets, requests, f, parentDataTypes, clearParentData);
        }

        @Override
        PeerRequestor withClearParentData(boolean clearParentData) {
            return new CreatePeersForMap<>(widgets, requests, f, interestedParentDataTypes, clearParentData);
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            WidgetState<?> thisWidgetState = widgetState();
            int reqCount = widgets.size();

            if (reqs == null)
                reqs = HashMap.newHashMap(reqCount);
            else
                this.reqs.keySet().retainAll(widgets.keySet());

            WidgetInstantiation[] children = new WidgetInstantiation[reqCount + 1];

            WidgetState<?> finisher = (children[reqCount] = thisWidgetState.tree.findOrCreateWidgetState(
                    new MapRRFinisher<>(reqs, f),
                    thisWidgetState,
                    existingChildren == null ? null : existingChildren[existingChildren.length - 1],
                    null,
                    false
            )).child();

            int i = 0;

            // TODO jelenteni kéne IntelliJ-seknek, hogy Map.forEach-ben lévő lambda esetén
            //      nem látszik a "Replace lambda with anonymous class"
            // TODO JDK-nak jelenteni kéne, hogy MapN.forEach feleslegesen vacakol entrySettel, miközben
            //      egy lapos Object[]-ben vannak a key/value párjai
            for (Map.Entry<K, ? extends Widget> entry : slots.with(widgets).entrySet()) {
                K key = entry.getKey();
                Widget widget = entry.getValue();
                Set<Request<P>> reqDatas = this.requests.get(key);

                Set<ResolutionRequest<P>> oldSet = reqs.getOrDefault(key, Collections.emptySet());
                Set<ResolutionRequest<P>> newSet = new HashSet<>();

                for (Request<P> req : reqDatas) {
                    ResolutionRequest<P> existing = oldSet.stream().
                            filter(rr -> Objects.equals(rr.requestData, req) &&
                                    Objects.equals(rr.widget, widget) &&
                                    Objects.equals(rr.interestedParentDataTypes, interestedParentDataTypes)).
                            findAny().orElse(null); // elvileg maximum 1 lehetséges
                    ResolutionRequest<P> rr;
                    if (existing == null) {
                        rr = new ResolutionRequest<>(
                                widgetState,
                                req, widget,
                                interestedParentDataTypes);
                        rr.finisherWidget = finisher;
                    } else
                        rr = existing;

                    boolean b = newSet.add(rr);
                    assert b;
                }
                reqs.put(key, newSet);

                // sorrend itt remélhetőleg mindegy
                children[i++] = thisWidgetState.tree.findOrCreateWidgetState(
                        widget,
                        widgetState,
                        null,
                        newSet,
                        clearParentData
                );
            }

            return children;
        }

        private static class MapRRFinisher<K, P> extends FinisherWidget {

            private final Map<K, Set<ResolutionRequest<P>>> reqs;
            private final Function<? super Map<Request<P>, Map<K, Result<P>>>, Widget> f;

            public MapRRFinisher(Map<K, Set<ResolutionRequest<P>>> reqs,
                                 Function<? super Map<Request<P>, Map<K, Result<P>>>, Widget> f) {
                this.reqs = reqs;
                this.f = f;
            }

            @Override
            protected Widget build() {
                Map<Request<P>, Map<K, Result<P>>> results = new HashMap<>();

                reqs.forEach((k, reqs) -> {
                    for (ResolutionRequest<P> req : reqs) {
                        results.computeIfAbsent(req.requestData, __ -> new HashMap<>()).
                                put(k, req.resultOrFail());
                    }
                });
                // TODO dokumentálni kéne, hogy f nem null-toleráns mapet kap
                return f.apply(Map.copyOf(results));
            }

            @Override
            String fToString() {
                return f.getClass().getName();
            }
        }
    }

    // csak toString miatt kell
    abstract static class FinisherWidget extends Widget {

        abstract String fToString();
    }
}
