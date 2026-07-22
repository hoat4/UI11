package ui11;

import org.jspecify.annotations.Nullable;
import ui11.PeerCreationRequest.ResolutionResult;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

abstract sealed class ResolutionRequestWidget extends Widget {

    /**
     * a finisher legyen utolsó, mert {@link #finisher()} függvény erre alapoz
     */
    abstract WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren);

    @Nullable WidgetState<?> finisher() {
        WidgetInstantiation[] a = (WidgetInstantiation[]) widgetState().children;
        WidgetInstantiation finisherWI = a[a.length - 1];
        if (finisherWI == null)
            return null;
        else
            return finisherWI.child();
    }

    @Override
    protected final Widget build() {
        throw new RuntimeException("should not reach here (RRW.b)");
    }

    static final class CreatePeerForSingle<P extends SubstitutedWidget> extends ResolutionRequestWidget {

        private final Widget widget;
        private final PeerCreationRequest<P> request;
        private final Function<ResolutionResult<P>, Widget> f;

        @Remember private ResolutionRequest<P> req;

        public CreatePeerForSingle(Widget widget, PeerCreationRequest<P> request, Function<ResolutionResult<P>, Widget> f) {
            this.widget = widget;
            this.request = request;
            this.f = f;
        }

        @Override
        WidgetInstantiation[] buildMulti(WidgetState<?> widgetState, WidgetInstantiation @Nullable [] existingChildren) {
            if (req == null || !Objects.equals(req.requestData, request) ||
                    !Objects.equals(req.widget, widget))
                req = new ResolutionRequest<>(
                        widgetState, widgetState.tree, request, widget);
            WidgetInstantiation reqW = widgetState.tree.findOrCreateWidgetState(
                    req.widget,
                    widgetState,
                    existingChildren == null ? null : existingChildren[0],
                    req,
                    Set.of());
            WidgetInstantiation finisher = widgetState.tree.findOrCreateWidgetState(
                    new SingleRRFinisher<>(req, f),
                    widgetState,
                    existingChildren == null ? null : existingChildren[1],
                    null,
                    Set.of());
            req.finisherWidget = finisher.child();
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

        @Remember private ResolutionRequest<P>[] reqs;

        public CreatePeersForList(List<? extends Widget> widgets,
                                  List<? extends PeerCreationRequest<P>> requests,
                                  Function<List<? extends ResolutionResult<P>>, Widget> f) {
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
                    null,
                    Set.of()
            )).child();

            for (int i = 0; i < widgets.size(); i++) {
                ResolutionRequest<P> req = reqs[i];
                if (req == null || !Objects.equals(req.requestData, this.requests.get(i)) ||
                        !Objects.equals(req.widget, widgets.get(i))) {
                    reqs[i] = req = new ResolutionRequest<>(
                            widgetState, widgetState.tree,
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
                        req,
                        Set.of()
                );
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

        @Remember private Map<K, ResolutionRequest<P>> reqs;

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
                    Set.of()
            )).child();

            int i = 0;

            // TODO jelenteni kéne IntelliJ-seknek, hogy Map.forEach-ben lévő lambda esetén
            //      nem látszik a "Replace lambda with anonymous class"
            // TODO JDK-nak jelenteni kéne, hogy MapN.forEach feleslegesen vacakol entrySettel, miközben
            //      egy lapos Object[]-ben vannak a key/value párjai
            for (Map.Entry<K, ? extends Widget> entry : widgets.entrySet()) {
                K key = entry.getKey();
                Widget widget = entry.getValue();
                Slot slot = slots.get(key);

                ResolutionRequest<P> req = reqs.get(key);

                if (req == null || !Objects.equals(req.requestData, this.requests.get(key)) ||
                        !Objects.equals(req.widget, widgets.get(key))) {
                    reqs.put(key, req = new ResolutionRequest<>(
                            widgetState, widgetState.tree,
                            requests.get(key), widget.withSlot(slot)));
                    req.finisherWidget = finisher;
                }

                // sorrend itt remélhetőleg mindegy
                children[i++] = thisWidgetState.tree.findOrCreateWidgetState(
                        req.widget,
                        widgetState,
                        null,
                        req,
                        Set.of()
                );
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
