package ui11;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public abstract class PeerCreationRequest<P extends SubstitutedWidget> {

    private final Class<P> peerType;

    // TODO ennek kéne számítania WidgetResolverek kiválasztásában?
    final Set<Class<? extends ParentDataWidget>> auxiliaryTypes;

    protected PeerCreationRequest(Class<P> peerType, Class<? extends ParentDataWidget>... auxiliaryTypes) {
        this.peerType = peerType;
        this.auxiliaryTypes = Set.of(auxiliaryTypes);
    }

    public final Class<P> peerType() {
        return peerType;
    }

    public final Widget executedOn(Widget widget, Function<ResolutionResult<P>, Widget> then) {
        return new ResolutionRequestWidget.CreatePeerForSingle(widget, this, then);
    }

    public final Widget executedOn(List<? extends Widget> widgets,
                                   Function<List<? extends ResolutionResult<P>>, Widget> then) {
        widgets = List.copyOf(widgets);
        return new ResolutionRequestWidget.CreatePeersForList<>(widgets, Collections.nCopies(widgets.size(), this), then);
    }

    public final <K> Widget executedOn(Map<K, ? extends Widget> widgets,
                                       Function<Map<K, ? extends ResolutionResult<P>>, Widget> then) {
        widgets = Map.copyOf(widgets);
        return new ResolutionRequestWidget.CreatePeersForMap<>(widgets,
                widgets.entrySet().stream().collect(toMap(
                        Map.Entry::getKey,
                        e -> this)),
                then);
    }

    public final Collector<Widget, ?, Widget> executing(Function<Stream<? extends ResolutionResult<P>>, Widget> then) {
        return Collectors.collectingAndThen(Collectors.toList(),
                list -> {
                    list = List.copyOf(list);
                    return new ResolutionRequestWidget.CreatePeersForList<>(
                            list,
                            Collections.nCopies(list.size(), this),
                            l -> then.apply(l.stream()));
                });
    }

    public static <P extends SubstitutedWidget> Widget executedMultipleOn(
            List<? extends Widget> widgets,
            List<? extends PeerCreationRequest<P>> requests,
            Function<List<? extends ResolutionResult<P>>, Widget> then) {

        widgets = List.copyOf(widgets);
        requests = List.copyOf(requests);
        Objects.requireNonNull(then);
        if (widgets.size() != requests.size())
            throw new IllegalArgumentException();

        return new ResolutionRequestWidget.CreatePeersForList<P>(widgets, requests, then);
    }

    public static final class ResolutionResult<P extends SubstitutedWidget> {

        private final @NonNull ResolutionRequest<P> req;
        private final @NonNull P peer;
        // TODO ennek a mapnek értelmesebb nevet kéne. parentDatas nem lehet, mert data már többes szám elvileg
        private final @NonNull Map<Class<? extends ParentDataWidget>, ParentDataWidget> parentDataList;

        ResolutionResult(@NonNull ResolutionRequest<P> req,
                         @NonNull P peer,
                         @NonNull Map<Class<? extends ParentDataWidget>, ParentDataWidget> parentDataList) {
            this.req = req;
            this.peer = peer;
            this.parentDataList = parentDataList;
        }

        /**
         *
         * @return the widget that the query was executed on
         */
        public Widget widget() {
            // TODO ha nincs slotja a widgethez, ennek hozzá kéne rendelnie egyet, ami a
            //      ResolutionRequestWidget megfelelő childjére mutat
            return req.widget();
        }

        public P peer() {
            return peer;
        }

        public Map<Class<? extends ParentDataWidget>, ParentDataWidget> parentDataList() {
            return parentDataList;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            ResolutionResult<?> that = (ResolutionResult<?>) o;
            return req.equals(that.req) && peer.equals(that.peer) && parentDataList.equals(that.parentDataList);
        }

        @Override
        public int hashCode() {
            int result = req.hashCode();
            result = 31 * result + peer.hashCode();
            result = 31 * result + parentDataList.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ResolutionResult{" +
                    "req=" + req +
                    ", peer=" + peer +
                    ", parentDataList=" + parentDataList +
                    '}';
        }
    }
}
