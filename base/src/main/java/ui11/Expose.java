package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A special widget which can be used to expose a value to ancestors.
 */
public final class Expose<P> extends Widget {

    private final @NonNull ExposeRequest<P> request;
    private final @NonNull P peer;
    private final @Nullable Widget chainedWidget;

    @Inject private ResolutionRequestCollection peerCreationRequestCollection;

    public Expose(@NonNull ExposeRequest<P> request, @NonNull P peer) {
        this(request, peer, null);
    }

    // lehet hogy jobb lenne ha utolsó paraméter is nonnull lenne
    public Expose(@NonNull ExposeRequest<P> request, @NonNull P peer,
                  @Nullable Widget chainedWidget) {
        Objects.requireNonNull(request);
        Objects.requireNonNull(peer);
        this.request = request;
        this.peer = peer;
        this.chainedWidget = chainedWidget;
    }

    @Override
    protected Widget build() {
        Map<ExposeRequest<?>, Set<ResolutionRequest<?>>> remaining = new HashMap<>();
        for (ResolutionRequest<?> resolutionRequest : peerCreationRequestCollection.requests()) {
            if (resolutionRequest.requestData.peerType().isInstance(peer) &&
                    request.equals(resolutionRequest.requestData)) {
                // TODO ha már kapott resultot ebben a refreshben, akkor az újabbakat ignorálnia kéne vagy beraknia?
                resolutionRequest.setResult(peer);
            } else {
                remaining.computeIfAbsent(resolutionRequest.requestData, __ -> new HashSet<>()).add(resolutionRequest);
            }
        }

        if (remaining.keySet().stream().allMatch(req -> req.defaultValue() != null)) {
            // ugyanaz mint SubstitutedWidget elején
            remaining.forEach((req, resReqs) -> {
                for (ResolutionRequest<?> resReq : resReqs)
                    resReq.setResult(req.defaultValue());
            });
            return new WidgetTree.ChainEnd();
        }

        if (chainedWidget == null)
            throw new RuntimeException("TODO remaining reqs: " + remaining);

        return ExposeRequest.requestOnSingleWidget(chainedWidget, remaining.keySet(), respMap -> {
            remaining.forEach((req, resReqs) -> {
                Object result2 = respMap.get(req);
                assert result2 != null;

                for (ResolutionRequest<?> resReq : resReqs) {
                    // TODO lásd fenti kommentek
                    resReq.setResult(result2);
                }
            });
            return new WidgetTree.ChainEnd();
        });
    }
}
