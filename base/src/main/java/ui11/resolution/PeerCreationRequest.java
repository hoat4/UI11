package ui11.resolution;

import ui11.EndingWidget;
import ui11.MultiSlot;
import ui11.Slot;
import ui11.Widget;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public abstract class PeerCreationRequest<P extends EndingWidget> {

    private final Class<P> peerType;

    protected PeerCreationRequest(Class<P> peerType) {
        this.peerType = peerType;
    }

    public final Class<P> peerType() {
        return peerType;
    }

    public final Widget executedOn(Widget widget, Function<P, Widget> then) {
        return new CreatePeerForSingle(widget, then);
    }

    public final Widget executedOn(List<? extends Widget> widgets, Function<List<? extends P>, Widget> then) {
        widgets = List.copyOf(widgets);
        return new CreatePeersForList<>(widgets, Collections.nCopies(widgets.size(), this), then);
    }

    public final <K> Widget executedOn(Map<K, ? extends Widget> widgets,
                                       Function<Map<K, ? extends P>, Widget> then) {
        widgets = Map.copyOf(widgets);
        return new CreatePeersForMap<>(widgets,
                widgets.entrySet().stream().collect(toMap(
                        Map.Entry::getKey,
                        e -> this)),
                then);
    }

    public final Collector<Widget, ?, Widget> executing(Function<Stream<? extends P>, Widget> then) {
        return Collectors.collectingAndThen(Collectors.toList(),
                list -> {
                    list = List.copyOf(list);
                    return new CreatePeersForList<>(
                            list,
                            Collections.nCopies(list.size(), this),
                            l -> then.apply(l.stream()));
                });
    }

    public static <P extends EndingWidget> Widget executedMultipleOn(
            List<? extends Widget> widgets,
            List<? extends PeerCreationRequest<P>> requests,
            Function<List<? extends P>, Widget> then) {

        widgets = List.copyOf(widgets);
        requests = List.copyOf(requests);
        Objects.requireNonNull(then);
        if (widgets.size() != requests.size())
            throw new IllegalArgumentException();

        return new CreatePeersForList<P>(widgets, requests, then);
    }

    private class CreatePeerForSingle extends Widget {

        private final Widget widget;
        private final Function<P, Widget> f;

        @Inject private Slot slot;

        public CreatePeerForSingle(Widget widget, Function<P, Widget> f) {
            this.widget = widget;
            this.f = f;
        }

        @Override
        protected Widget build() {
            P p = internal_makePeer(slot, widget, PeerCreationRequest.this);
            return f.apply(p);
        }
    }

    private static class CreatePeersForList<P extends EndingWidget> extends Widget {

        private final List<? extends Widget> widgets;
        private final List<? extends PeerCreationRequest<P>> requests;
        private final Function<List<? extends P>, Widget> f;

        @Inject private MultiSlot<Integer> slots;

        public CreatePeersForList(List<? extends Widget> widgets,
                                  List<? extends PeerCreationRequest<P>> requests,
                                  Function<List<? extends P>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        protected Widget build() {
            EndingWidget[] peers = new EndingWidget[widgets.size()];
            for (int i = 0; i < widgets.size(); i++)
                peers[i] = internal_makePeer(slots.get(i), widgets.get(i), requests.get(i));

            @SuppressWarnings("unchecked")
            List<P> castedList = (List<P>) List.of(peers);
            return f.apply(castedList);
        }
    }

    private static class CreatePeersForMap<P extends EndingWidget, K> extends Widget {

        private final Map<K, ? extends Widget> widgets;
        private final Map<K, ? extends PeerCreationRequest<P>> requests;
        private final Function<Map<K, ? extends P>, Widget> f;

        @Inject private MultiSlot<K> slots;

        public CreatePeersForMap(Map<K, ? extends Widget> widgets,
                                 Map<K, ? extends PeerCreationRequest<P>> requests,
                                 Function<Map<K, ? extends P>, Widget> f) {
            this.widgets = widgets;
            this.requests = requests;
            this.f = f;
        }

        @Override
        protected Widget build() {
            Map<K, EndingWidget> peers = new HashMap<>();
            widgets.forEach((k, w) -> {
                peers.put(k, internal_makePeer(slots.get(k), w, requests.get(k)));
            });

            @SuppressWarnings("unchecked")
            Map<K, P> castedMap = (Map<K, P>) Map.copyOf(peers);
            return f.apply(castedMap);
        }
    }
}
