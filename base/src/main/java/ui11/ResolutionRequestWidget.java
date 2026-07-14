package ui11;

import org.jspecify.annotations.Nullable;
import ui11.PeerCreationRequest.ResolutionResult;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

abstract sealed class ResolutionRequestWidget extends Widget {

    // TODO ehhez nem is kéne WidgetState (kivéve a Mapes változathoz),
    //      csak akkor csinálni kéne valamit az egymásba ágyazottakkal.
    //      mivel úgyis ritka (még nem láttam rá use caset), ezért
    //      lehet pl. csak bewrappelni egy másik widgetbe, aminek már van WidgetStateje.

    abstract WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren);

    @Override
    protected final Widget build() {
        throw new RuntimeException("should not reach here (RRW.b)");
    }

    static final class CreatePeerForSingle<P extends SubstitutedWidget> extends ResolutionRequestWidget {

        private final Widget widget;
        private final PeerCreationRequest<P> request;
        private final Function<ResolutionResult<P>, Widget> f;

        public CreatePeerForSingle(Widget widget, PeerCreationRequest<P> request, Function<ResolutionResult<P>, Widget> f) {
            this.widget = widget;
            this.request = request;
            this.f = f;
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            ResolutionRequest<P> req = new ResolutionRequest<>(
                    widgetState, widgetState.tree, request, widget);
            WidgetInstantiation reqW = widgetState.tree.findOrCreateWidgetState(
                    req.primaryWrapper(),
                    widgetState,
                    existingChildren == null ? null : existingChildren[0]);
            req.setWidgetInstantiation(reqW);
            WidgetInstantiation finisher = widgetState.tree.findOrCreateWidgetState(
                    new SingleRRFinisher<>(req, f),
                    widgetState,
                    existingChildren == null ? null : existingChildren[1]);
            req.finisherWidget = finisher.widgetState();
            return new WidgetInstantiation[]{reqW, finisher};
        }

        private static class SingleRRFinisher<P extends SubstitutedWidget> extends Widget {

            private final ResolutionRequest<P> req;
            private final Function<ResolutionResult<P>, Widget> f;

            public SingleRRFinisher(ResolutionRequest<P> req, Function<ResolutionResult<P>, Widget> f) {
                this.req = req;
                this.f = f;
            }

            @Override
            protected Widget build() {
                return f.apply(req.resultOrFail());
            }
        }
    }

    static final class CreatePeersForList<P extends SubstitutedWidget> extends ResolutionRequestWidget {

        private final List<? extends Widget> widgets;
        private final List<? extends PeerCreationRequest<P>> requests;
        private final Function<List<? extends ResolutionResult<P>>, Widget> f;

        public CreatePeersForList(List<? extends Widget> widgets,
                                  List<? extends PeerCreationRequest<P>> requests,
                                  Function<List<? extends ResolutionResult<P>>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            @SuppressWarnings("unchecked")
            ResolutionRequest<P>[] requests = new ResolutionRequest[widgets.size()];
            WidgetInstantiation[] children = new WidgetInstantiation[widgets.size() + 1];

            WidgetState<?> finisher = (children[widgets.size()] = widgetState.tree.findOrCreateWidgetState(
                    new ListRRFinisher<>(requests, f),
                    widgetState,
                    existingChildren == null ? null : existingChildren[existingChildren.length - 1]
            )).widgetState();

            for (int i = 0; i < widgets.size(); i++) {
                ResolutionRequest<P> req = new ResolutionRequest<>(
                        widgetState, widgetState.tree,
                        this.requests.get(i), widgets.get(i));
                req.finisherWidget = finisher;
                requests[i] = req;
                WidgetInstantiation existingWidgetState =
                        existingChildren != null && existingChildren.length - 1 > i ?
                                existingChildren[i] : null;
                req.setWidgetInstantiation(children[i] = widgetState.tree.findOrCreateWidgetState(
                        req.primaryWrapper(),
                        widgetState,
                        existingWidgetState
                ));
            }

            return children;
        }

        private static class ListRRFinisher<P extends SubstitutedWidget> extends Widget {

            private final ResolutionRequest<P>[] reqs;
            private final Function<List<? extends ResolutionResult<P>>, Widget> f;

            public ListRRFinisher(ResolutionRequest<P>[] reqs,
                                  Function<List<? extends ResolutionResult<P>>, Widget> f) {
                this.reqs = reqs;
                this.f = f;
            }

            @Override
            protected Widget build() {
                @SuppressWarnings("unchecked")
                ResolutionResult<P>[] results = new ResolutionResult[reqs.length];
                for (int i = 0; i < results.length; i++)
                    results[i] = reqs[i].resultOrFail();
                // TODO dokumentálni kéne, hogy f nem null-toleráns mapet kap
                return f.apply(List.of(results));
            }
        }
    }

    static final class CreatePeersForMap<P extends SubstitutedWidget, K> extends ResolutionRequestWidget {

        private final Map<K, ? extends Widget> widgets;
        private final Map<K, ? extends PeerCreationRequest<P>> requests;
        private final Function<Map<K, ? extends ResolutionResult<P>>, Widget> f;

        @Inject private MultiSlot<K> slots;

        public CreatePeersForMap(Map<K, ? extends Widget> widgets,
                                 Map<K, ? extends PeerCreationRequest<P>> requests,
                                 Function<Map<K, ? extends ResolutionResult<P>>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            WidgetState<?> thisWidgetState = widgetState();
            int reqCount = widgets.size();
            Map<K, ResolutionRequest<P>> reqs = HashMap.newHashMap(reqCount);

            WidgetInstantiation[] children = new WidgetInstantiation[reqCount + 1];

            WidgetState<?> finisher = (children[reqCount] = thisWidgetState.tree.findOrCreateWidgetState(
                    new MapRRFinisher<>(reqs, f),
                    thisWidgetState,
                    existingChildren == null ? null : existingChildren[existingChildren.length - 1]
            )).widgetState();

            int i = 0;

            // TODO jelenteni kéne IntelliJ-seknek, hogy Map.forEach-ben lévő lambda esetén
            //      nem látszik a "Replace lambda with anonymous class"
            // TODO JDK-nak jelenteni kéne, hogy MapN.forEach feleslegesen vacakol entrySettel, miközben
            //      egy lapos Object[]-ben vannak a key/value párjai
            for (Map.Entry<K, ? extends Widget> entry : widgets.entrySet()) {
                K key = entry.getKey();
                Widget widget = entry.getValue();
                Slot slot = slots.get(key);

                ResolutionRequest<P> req = new ResolutionRequest<>(
                        widgetState, widgetState.tree,
                        requests.get(key), widget.withSlot(slot));
                req.finisherWidget = finisher;
                reqs.put(key, req);

                // sorrend itt remélhetőleg mindegy
                req.setWidgetInstantiation(children[i++] = thisWidgetState.tree.findOrCreateWidgetState(
                        req.primaryWrapper(),
                        widgetState,
                        null
                ));
            }

            return children;
        }

        private static class MapRRFinisher<K, P extends SubstitutedWidget> extends Widget {

            private final Map<K, ResolutionRequest<P>> reqs;
            private final Function<Map<K, ? extends ResolutionResult<P>>, Widget> f;

            public MapRRFinisher(Map<K, ResolutionRequest<P>> reqs,
                                 Function<Map<K, ? extends ResolutionResult<P>>, Widget> f) {
                this.reqs = reqs;
                this.f = f;
            }

            @Override
            protected Widget build() {
                @SuppressWarnings("unchecked")
                Map.Entry<K, ResolutionResult<P>>[] results = new Map.Entry[reqs.size()];
                reqs.forEach(new BiConsumer<>() {

                    private int i = 0;

                    @Override
                    public void accept(K k, ResolutionRequest<P> req) {
                        results[i++] = Map.entry(k, req.resultOrFail());
                    }
                });
                // TODO dokumentálni kéne, hogy f nem null-toleráns mapet kap
                return f.apply(Map.ofEntries(results));
            }
        }
    }
}
