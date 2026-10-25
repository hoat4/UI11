package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * @param requests the map key is {@linkplain ExposeRequest#peerType() peer type}
 */
record ResolutionRequestCollection(@NonNull Set<? extends ResolutionRequest<?>> requests) {

    ResolutionRequestCollection {
        requests = Set.copyOf(requests);

        Map<Class<? extends ExposeRequest<?>>, ResolutionRequest<?>> byBase = null;
        for (ResolutionRequest<?> req : requests) {
            Class<? extends ExposeRequest<?>> base = req.metadata().baseForAtMostOnce;
            if (base == null)
                continue;
            if (byBase == null)
                byBase = new HashMap<>();
            if (byBase.put(base, req) != null) {
                List<? extends ResolutionRequest<?>> withSameBase = requests.stream().
                    filter(req2 -> req2.metadata().baseForAtMostOnce == base).
                    toList();
                // TODO print other duplicates as well
                throw new RuntimeException("Duplicate " + base.getSimpleName() + " found: " + withSameBase);
            }
        }
    }

    @Override
    public @NonNull String toString() {
        return getClass().getSimpleName() + requests;
    }

    public List<? extends ResolutionRequest<?>> byType(Class<? extends ExposeRequest<?>> type) {
        return requests.stream().
                filter(r -> type.isInstance(r.requestData)).
                toList();
    }
}
