package ui11;

import ui11.PeerCreationRequest.ResolutionResult;
import ui11.observable.MutableObservable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResolutionRequest<P extends SubstitutedWidget> {

    final PeerCreationRequest<P> requestData;
    final MutableObservable<ResolutionResult<P>> result = MutableObservable.ofNullable();

    public ResolutionRequest(PeerCreationRequest<P> requestData) {
        this.requestData = requestData;
    }

    void setResultUnchecked(SubstitutedWidget peer, List<? extends ParentDataWidget> parentDataWidgets) {
        Map<Class<? extends ParentDataWidget>, ParentDataWidget> parentDataMap = new HashMap<>();
        for (Class<? extends ParentDataWidget> type : requestData.auxiliaryTypes) {
            for (ParentDataWidget w : parentDataWidgets)
                if (type.isInstance(w)) {
                    parentDataMap.put(type, type.cast(w));
                    break;
                }
        }

        @SuppressWarnings("unchecked") final P castedPeer = (P) peer;
        this.result.set(new ResolutionResult<>(castedPeer, Map.copyOf(parentDataMap)));
    }

    static final class ResolutionRequestCollection {

        // for now, only one request at a time
        public final ResolutionRequest<?> request;

        public ResolutionRequestCollection(ResolutionRequest<?> request) {
            this.request = request;
        }
    }
}
