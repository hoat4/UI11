package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.PeerCreationRequest.ResolutionResult;
import ui11.observable.MutableObservable;
import ui11.provide.Provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ResolutionRequest<P extends SubstitutedWidget> {

    final @Nullable WidgetState<?> container;
    final @NonNull Widget widget;
    final @NonNull PeerCreationRequest<P> requestData;

    /**
     * amikor ezt megváltoztatjuk, írjuk át {@link #resultSetAt}-et is
     */
    final @NonNull MutableObservable<@Nullable ResolutionResult<P>> result =
            MutableObservable.ofNullable();
    long resultSetAt;

    WidgetInstantiation reqWI;
    @Nullable WidgetState<?> finisherWidget;

    /**
     * létrehozás után állítsuk be {@link #finisherWidget}-et ha van (rooton kívül mindig van),
     * és {@link #reqWI}-et
     *
     * @param container root esetén null
     */
    public ResolutionRequest(
            @Nullable WidgetState<?> container,
            @NonNull WidgetTree tree,
            @NonNull PeerCreationRequest<P> requestData,
            @NonNull Widget widget
    ) {
        this.container = container;
        this.requestData = Objects.requireNonNull(requestData);
        this.widget = widget;
    }

    void setResultUnchecked(@NonNull SubstitutedWidget peer,
                            @NonNull List<? extends ParentDataWidget> parentDataWidgets,
                            long refreshID) {
        Objects.requireNonNull(peer);
        Objects.requireNonNull(parentDataWidgets);

        Map<Class<? extends ParentDataWidget>, ParentDataWidget> parentDataMap = new HashMap<>();
        for (Class<? extends ParentDataWidget> type : requestData.auxiliaryTypes) {
            for (ParentDataWidget w : parentDataWidgets)
                if (type.isInstance(w)) {
                    parentDataMap.put(type, type.cast(w));
                    break;
                }
        }

        @SuppressWarnings("unchecked") final P castedPeer = (P) peer;
        this.result.set(new ResolutionResult<>(this, castedPeer, Map.copyOf(parentDataMap)));
        resultSetAt = refreshID;
    }

    ResolutionResult<P> resultOrFail() {
        ResolutionResult<P> value = result.get();
        if (value == null)
            throw makeResolutionFailedException();
        else
            return value;
    }

    private RuntimeException makeResolutionFailedException() {
        StringBuilder sb = new StringBuilder("Resolution failed for " + this);
        // TODO itt az exception messagenek más formátuma van mint a másiknak
        WidgetState<?> w = reqWI.child();
        while (true) {
            sb.append("\n- ").append(String.valueOf(w.stateWidget).replace("\n", "\n  "));
            if (w.stateWidget instanceof ResolutionRequestWidget || w.children == null)
                break;
            else
                w = ((WidgetInstantiation) w.children).child();
        }
        return new RuntimeException(sb.toString());
    }

    private static <T> Provider<T> wrapWithProvide(Map.Entry<Class<?>, Object> e, Widget w) {
        @SuppressWarnings("unchecked") Class<T> key = (Class<T>) e.getKey();
        return new Provider<>(key, key.cast(e.getValue()), w);
    }

    Widget widget() {
        return widget;
    }

    @Override
    public String toString() {
        return super.toString() + " [requestData=" + requestData + "]";
    }

    static final class ResolutionRequestCollection {

        /**
         * key: {@linkplain PeerCreationRequest#peerType() peer type}
         */
        public final Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests;

        public ResolutionRequestCollection(ResolutionRequest<?> initialRequest) {
            this.requests = Map.of(initialRequest.requestData.peerType(), initialRequest);
        }

        private ResolutionRequestCollection(Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests) {
            this.requests = requests;
        }

        public static ResolutionRequestCollection of(List<ResolutionRequest<?>> reqs) {
            Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests = new HashMap<>();
            reqs.forEach(req->{
                if (requests.putIfAbsent(req.requestData.peerType(), req) != null)
                    throw new RuntimeException("Multiple requests with peer type " + req.requestData.peerType());
            });
            return new ResolutionRequestCollection(requests);
        }

        // TODO equals?

        @NonNull ResolutionRequestCollection combineWith(@NonNull ResolutionRequestCollection c) {
            Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests = new HashMap<>(this.requests);
            c.requests.forEach((peerType, req) -> {
                if (requests.putIfAbsent(peerType, req) != null)
                    throw new RuntimeException("Multiple requests with peer type " + peerType.getName());
            });
            return new ResolutionRequestCollection(requests);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + requests.toString();
        }
    }
}
