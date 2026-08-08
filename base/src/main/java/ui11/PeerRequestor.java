package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toUnmodifiableMap;

/**
 * A widget that requests some information about some child widgets, then invokes a callback with the results.
 * <p>
 * The requests can be accessed in the following ways:
 * <ul>
 *     <li>By implementing the {@link WidgetResolver#tryResolveRequestSpecific(SubstitutedWidget, PeerRequest)} method,
 *     which will be called for each request
 *     <li>Declaring a {@link ui11.Widget.Inject @Inject} field in a widget whose type is the array type of the request
 *     type. The array's value will be the current peer requests that should be fulfilled. The array won't contain two
 *     {@linkplain Object#equals(Object) same} element twice.
 * </ul>
 * A widget can provide response to a request by returning the value of
 * {@linkplain PeerRequest#createResponse(Object) Request.createResponse} or
 * {@linkplain PeerRequest#createResponse(Object, Widget)}.
 */
abstract sealed class PeerRequestor extends Widget {

    @Override
    protected Widget build() {
        throw new RuntimeException("should not reach here (PR.b)");
    }

    /**
     * @return ha null, ha nincs child. üres tömb nem lehet.
     */
    abstract WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren);

    static final class ResponseWidget<P> extends Widget {

        private final @NonNull PeerRequest<P> request;
        private final @NonNull P peer;
        private final @Nullable Widget chainedWidget;

        @Inject private ResolutionRequestCollection peerCreationRequestCollection;

        public ResponseWidget(@NonNull PeerRequest<P> request, @NonNull P peer, @Nullable Widget chainedWidget) {
            this.request = request;
            this.peer = peer;
            this.chainedWidget = chainedWidget;
        }

        @Override
        protected Widget build() {
            Map<PeerRequest<?>, ResolutionRequest<?>> remaining = new HashMap<>();
            for (ResolutionRequest<?> resolutionRequest : peerCreationRequestCollection.requests()) {
                if (resolutionRequest.requestData.peerType().isInstance(peer) &&
                        request.equals(resolutionRequest.requestData)) {
                    // TODO ha már kapott resultot ebben a refreshben, akkor az újabbakat ignorálnia kéne vagy beraknia?
                    resolutionRequest.setResult(peer);
                } else {
                    if (remaining.put(resolutionRequest.requestData, resolutionRequest) != null)
                        // több ResolutionRequest tartozik egy Requesthez
                        throw new RuntimeException("TODO");
                }
            }

            if (remaining.keySet().stream().allMatch(req -> req.defaultValue() != null)) {
                // ugyanaz mint SubstitutedWidget elején
                remaining.forEach((req, resolutionRequest) -> {
                    resolutionRequest.setResult(req.defaultValue());
                });
                return new WidgetTree.ChainEnd();
            }

            if (chainedWidget == null)
                throw new RuntimeException("TODO");

            return PeerRequest.requestOnSingleWidget(chainedWidget, remaining.keySet(), respMap -> {
                remaining.forEach((req, resReq) -> {
                    Object result2 = respMap.get(req);
                    assert result2 != null;

                    // TODO lásd fenti kommentek
                    resReq.setResult(result2);
                });
                return new WidgetTree.ChainEnd();
            });
        }
    }

    static final class CreatePeerForSingle<P> extends PeerRequestor {

        private final Widget widget;
        private final PeerRequest<P> request;
        private final Function<P, Widget> f;

        @Remember private ResolutionRequest<P> req;

        @NullMarked
        public CreatePeerForSingle(Widget widget, PeerRequest<P> request, Function<P, Widget> f) {
            this.widget = widget;
            this.request = request;
            this.f = f;
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            if (req == null || !Objects.equals(req.requestData, request) ||
                    !Objects.equals(req.widget, widget))
                req = new ResolutionRequest<>(
                        widgetState, request, widget);
            WidgetInstantiation reqW = widgetState.tree.findOrCreateWidgetState(
                    req.widget,
                    widgetState,
                    existingChildren == null ? null : existingChildren[0],
                    Set.of(req)
            );
            WidgetInstantiation finisher = widgetState.tree.findOrCreateWidgetState(
                    new SingleRRFinisher<>(req, f),
                    widgetState,
                    existingChildren == null ? null : existingChildren[1],
                    null
            );
            req.finisherWidget = finisher.child();
            return new WidgetInstantiation[]{reqW, finisher};
        }

        private static class SingleRRFinisher<P> extends FinisherWidget {

            private final ResolutionRequest<P> req;
            private final Function<P, Widget> f;

            public SingleRRFinisher(ResolutionRequest<P> req, Function<P, Widget> f) {
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
        private final List<? extends PeerRequest<P>> requests;
        private final Function<? super List<P>, Widget> f;

        @Remember private ResolutionRequest<P>[] reqs;

        public CreatePeersForList(List<? extends Widget> widgets,
                                  List<? extends PeerRequest<P>> requests,
                                  Function<? super List<P>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
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
                    null
            )).child();

            for (int i = 0; i < widgets.size(); i++) {
                ResolutionRequest<P> req = reqs[i];
                if (req == null || !Objects.equals(req.requestData, this.requests.get(i)) ||
                        !Objects.equals(req.widget, widgets.get(i))) {
                    reqs[i] = req = new ResolutionRequest<>(
                            widgetState,
                            this.requests.get(i), widgets.get(i));
                    req.finisherWidget = finisher;
                }
                WidgetInstantiation existingWidgetState =
                        existingChildren != null && existingChildren.length - 1 > i ?
                                existingChildren[i] : null;
                children[i] = widgetState.tree.findOrCreateWidgetState(
                        req.widget,
                        widgetState,
                        existingWidgetState,
                        Set.of(req)
                );
            }

            return children;
        }

        private static class ListRRFinisher<P> extends FinisherWidget {

            private final ResolutionRequest<P>[] reqs;
            private final Function<? super List<P>, Widget> f;

            public ListRRFinisher(ResolutionRequest<P>[] reqs,
                                  Function<? super List<P>, Widget> f) {
                this.reqs = reqs;
                this.f = f;
            }

            @Override
            protected Widget build() {
                Object[] results = new Object[reqs.length];
                for (int i = 0; i < results.length; i++)
                    results[i] = reqs[i].resultOrFail();
                // TODO dokumentálni kéne, hogy f nem null-toleráns Listet kap
                List<?> objList = List.of(results);
                @SuppressWarnings("unchecked") List<P> castedList = (List<P>) (List<?>) objList;
                return f.apply(castedList);
            }

            @Override
            String fToString() {
                return f.getClass().getName();
            }
        }
    }

    static final class CreatePeersForMap<P, K> extends PeerRequestor {

        private final Map<K, ? extends Widget> widgets;
        private final Map<K, ? extends Set<PeerRequest<P>>> requests;
        private final Function<? super Map<PeerRequest<P>, Map<K, P>>, Widget> f;

        @Remember private Key.KeyCache<K> slots;
        @Remember private Map<K, Set<ResolutionRequest<P>>> reqs;

        public CreatePeersForMap(Map<K, ? extends Widget> widgets,
                                 Map<K, ? extends Set<PeerRequest<P>>> requests,
                                 Function<? super Map<PeerRequest<P>, Map<K, P>>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        protected void initState() {
            slots = new Key.KeyCache<>();
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
                    null
            )).child();

            int i = 0;

            // TODO jelenteni kéne IntelliJ-seknek, hogy Map.forEach-ben lévő lambda esetén
            //      nem látszik a "Replace lambda with anonymous class"
            // TODO JDK-nak jelenteni kéne, hogy MapN.forEach feleslegesen vacakol entrySettel, miközben
            //      egy lapos Object[]-ben vannak a key/value párjai
            for (Map.Entry<K, ? extends Widget> entry : slots.with(widgets).entrySet()) {
                K key = entry.getKey();
                Widget widget = entry.getValue();
                Set<PeerRequest<P>> reqDatas = this.requests.get(key);

                Set<ResolutionRequest<P>> oldSet = reqs.getOrDefault(key, Collections.emptySet());
                Set<ResolutionRequest<P>> newSet = new HashSet<>();

                for (PeerRequest<P> req : reqDatas) {
                    ResolutionRequest<P> existing = oldSet.stream().
                            filter(rr -> Objects.equals(rr.requestData, req) &&
                                    Objects.equals(rr.widget, widget)).
                            findAny().orElse(null); // elvileg maximum 1 lehetséges
                    ResolutionRequest<P> rr;
                    if (existing == null) {
                        rr = new ResolutionRequest<>(
                                widgetState,
                                req, widget);
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
                        newSet
                );
            }

            return children;
        }

        private static class MapRRFinisher<K, P> extends FinisherWidget {

            private final Map<K, Set<ResolutionRequest<P>>> reqs;
            private final Function<? super Map<PeerRequest<P>, Map<K, P>>, Widget> f;

            public MapRRFinisher(Map<K, Set<ResolutionRequest<P>>> reqs,
                                 Function<? super Map<PeerRequest<P>, Map<K, P>>, Widget> f) {
                this.reqs = reqs;
                this.f = f;
            }

            @Override
            protected Widget build() {
                Map<PeerRequest<P>, Map<K, P>> results = new HashMap<>();

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
