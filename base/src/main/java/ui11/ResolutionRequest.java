package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.ParentDataWidget.ParentData;
import ui11.PeerRequestor.Result;
import ui11.observable.MutableObservable;
import ui11.provide.Provider;

import java.util.*;

class ResolutionRequest<P> {

    final @Nullable WidgetState<?> container;
    final @NonNull Widget widget;
    final PeerRequestor.@NonNull Request<P> requestData;
    final Set<Class<? extends ParentData>> interestedParentDataTypes;

    final @NonNull MutableObservable<@Nullable Result<P>> result =
            MutableObservable.ofNullable();

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
            PeerRequestor.@NonNull Request<P> requestData,
            @NonNull Widget widget,
            Set<Class<? extends ParentData>> interestedParentDataTypes
    ) {
        this.container = container;
        this.requestData = Objects.requireNonNull(requestData);
        this.widget = widget;
        this.interestedParentDataTypes = interestedParentDataTypes;
    }

    void setResultUnchecked(@NonNull Object peer,
                            @NonNull List<? extends ParentDataWidget> parentDataWidgets,
                            long refreshID) {
        Objects.requireNonNull(peer);
        Objects.requireNonNull(parentDataWidgets);

        Map<Class<? extends ParentData>, ParentData> parentDataMap = new HashMap<>();
        for (Class<? extends ParentData> type : interestedParentDataTypes) {
            for (ParentDataWidget w : parentDataWidgets)
                if (type.isInstance(w.parentData)) {
                    parentDataMap.put(type, w.parentData);
                    break;
                }
        }

        @SuppressWarnings("unchecked") final P castedPeer = (P) peer;
        this.result.set(new PeerRequestor.Result<>(this, castedPeer, Map.copyOf(parentDataMap)));
    }

    void setResultFrom(Result<? /*P*/> other) {
        if (!Objects.equals(other.req.requestData, requestData))
            // ha ez a feltétel nem teljesül, akkor nem biztonságos a lenti cast
            throw new IllegalArgumentException();

        @SuppressWarnings("unchecked")
        Result<P> castedResult = (Result<P>) other;
        // valszeg másik ResolutionRequestből származik, ezért req-t átállítjuk this-re
        this.result.set(new PeerRequestor.Result<>(this, castedResult.peer(), castedResult.parentDataList()));
    }

    Result<P> resultOrFail() {
        Result<P> value = result.get();
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
            if (w.stateWidget instanceof PeerRequestor || w.children == null)
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
}
