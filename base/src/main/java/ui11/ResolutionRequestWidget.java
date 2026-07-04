package ui11;

import ui11.provide.Provider;

import java.util.*;
import java.util.function.Function;

abstract sealed class ResolutionRequestWidget<P> extends Widget {

    /**
     * A delegate láncon végighaladva keres egy olyan UpValuet, mely típusa a megadott típus vagy annak egy altípusa, és
     * visszaadja azt. Ha több ilyen is van, akkor a legelsőt.
     */
    <U extends SubstitutedWidget> PeerCreationRequest.ResolutionResult<U> useWidget(
            Slot defaultSlot, Widget widget, PeerCreationRequest<U> request) {
        Objects.requireNonNull(defaultSlot);
        Objects.requireNonNull(widget);
        Objects.requireNonNull(request);

        ResolutionRequest<U> req = new ResolutionRequest<>(request);

        widget = new Provider<>(ResolutionRequest.ResolutionRequestCollection.class,
                new ResolutionRequest.ResolutionRequestCollection(req), widget);
        widget = new Provider<>(ParentDataWidget.ParentDataCollection.class,
                ParentDataWidget.ParentDataCollection.CLEAR, widget);

        if (Element.TRACE_REFRESH)
            Element.TraceRefresh.TL.get().print("useWidget " + request + ": " + widget);

        Element element = element();
        element.instantiate(defaultSlot, widget).ensureFresh();

        PeerCreationRequest.ResolutionResult<U> result = req.result.get();
        // TODO ha nem tud továbbhaladni a delegate láncon a refresh, akkor azt valahogy jelezni kéne itt is gondolom
        if (result == null)
            throw new RuntimeException("Resolution failed for " + widget + ", " + request);
        else
            return result;
    }

    static final class CreatePeerForSingle<P extends SubstitutedWidget> extends ResolutionRequestWidget<P> {

        private final Widget widget;
        private final PeerCreationRequest<P> request;
        private final Function<PeerCreationRequest.ResolutionResult<P>, Widget> f;

        @Inject private Slot slot;

        public CreatePeerForSingle(Widget widget, PeerCreationRequest<P> request, Function<PeerCreationRequest.ResolutionResult<P>, Widget> f) {
            this.widget = widget;
            this.request = request;
            this.f = f;
        }

        @Override
        protected Widget build() {
            PeerCreationRequest.ResolutionResult<P> p = useWidget(slot, widget, request);
            return new Finisher<>(p, f);
        }
    }

    static final class CreatePeersForList<P extends SubstitutedWidget> extends ResolutionRequestWidget {

        private final List<? extends Widget> widgets;
        private final List<? extends PeerCreationRequest<P>> requests;
        private final Function<List<? extends PeerCreationRequest.ResolutionResult<P>>, Widget> f;

        @Inject private MultiSlot<Integer> slots;

        public CreatePeersForList(List<? extends Widget> widgets,
                                  List<? extends PeerCreationRequest<P>> requests,
                                  Function<List<? extends PeerCreationRequest.ResolutionResult<P>>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        protected Widget build() {
            @SuppressWarnings("unchecked")
            PeerCreationRequest.ResolutionResult<P>[] peers = new PeerCreationRequest.ResolutionResult[widgets.size()];
            for (int i = 0; i < widgets.size(); i++) {
                Slot defaultSlot = slots.get(i);
                peers[i] = useWidget(defaultSlot, widgets.get(i), requests.get(i));
            }

            // TODO dokumentálni kéne, hogy f nem null-toleráns mapet kap
            return new Finisher<>(List.of(peers), f);
        }
    }

    static final class CreatePeersForMap<P extends SubstitutedWidget, K> extends ResolutionRequestWidget<P> {

        private final Map<K, ? extends Widget> widgets;
        private final Map<K, ? extends PeerCreationRequest<P>> requests;
        private final Function<Map<K, ? extends PeerCreationRequest.ResolutionResult<P>>, Widget> f;

        @Inject private MultiSlot<K> slots;

        public CreatePeersForMap(Map<K, ? extends Widget> widgets,
                                 Map<K, ? extends PeerCreationRequest<P>> requests,
                                 Function<Map<K, ? extends PeerCreationRequest.ResolutionResult<P>>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        protected Widget build() {
            Map<K, PeerCreationRequest.ResolutionResult<P>> peers = new HashMap<>();
            widgets.forEach((k, w) -> {
                Slot defaultSlot = slots.get(k);
                peers.put(k, useWidget(defaultSlot, w, requests.get(k)));
            });

            return new Finisher<>(Map.copyOf(peers), f);
        }
    }

    // ResolutionRequestWidgetnek mindenképpen fut a refreshSelf-je, ezért f hívását
    // külön widgetbe hozzuk, hogy csak a szükséges esetekben fusson
    private static class Finisher<R> extends Widget {

        final R result;
        final Function<R, Widget> f;

        public Finisher(R result, Function<R, Widget> f) {
            this.result = result;
            this.f = f;
        }

        @Override
        protected Widget build() {
            return f.apply(result);
        }
    }
}
