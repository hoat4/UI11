package ui11;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @param requests the map key is {@linkplain ui11.PeerRequestor.Request#peerType() peer type}
 */
record ResolutionRequestCollection(@NonNull Set<? extends ResolutionRequest<?>> requests) {

    ResolutionRequestCollection {
        requests = Set.copyOf(requests);
    }

    static ResolutionRequestCollection combine(Set<ResolutionRequestCollection> requestCollections) {
        return new ResolutionRequestCollection(
                requestCollections.stream().flatMap(c -> c.requests.stream()).
                        collect(Collectors.toUnmodifiableSet()));
    }

    @Override
    public @NonNull String toString() {
        return getClass().getSimpleName() + requests;
    }

    public List<? extends ResolutionRequest<?>> byType(Class<? extends PeerRequestor.Request<?>> type) {
        return requests.stream().
                filter(r -> type.isInstance(r.requestData)).
                toList();
    }
}
