package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.PeerRequestor.Result;
import ui11.observable.MutableObservable;
import ui11.provide.Provider;

import java.util.*;

class ResolutionRequest<P> {

    final @Nullable WidgetState<?> container;
    final @NonNull Widget widget;
    final PeerRequestor.@NonNull Request<P> requestData;

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
            @NonNull Widget widget
    ) {
        this.container = container;
        this.requestData = Objects.requireNonNull(requestData);
        this.widget = widget;
    }

    void setResultUnchecked(@NonNull Object peer,
                            long refreshID) {
        Objects.requireNonNull(peer);

        @SuppressWarnings("unchecked") final P castedPeer = (P) peer;
        this.result.set(new PeerRequestor.Result<>(this, castedPeer));
    }

    void setResultFrom(Result<? /*P*/> other) {
        if (!Objects.equals(other.req.requestData, requestData))
            // ha ez a feltétel nem teljesül, akkor nem biztonságos a lenti cast
            throw new IllegalArgumentException();

        @SuppressWarnings("unchecked")
        Result<P> castedResult = (Result<P>) other;
        // valszeg másik ResolutionRequestből származik, ezért req-t átállítjuk this-re
        this.result.set(new PeerRequestor.Result<>(this, castedResult.peer()));
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

    @Override
    public String toString() {
        return super.toString() + " [requestData=" + requestData + "]";
    }
}
