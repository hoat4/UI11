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

        for (ResolutionRequest<?> req : requests) {
            Class<? extends ExposeRequest<?>> base = req.metadata().baseForAtMostOnce;
            if (base == null)
                continue;
            Set<ResolutionRequest<?>> duplicates = requests.stream().
                    filter(req2 -> req2.metadata().baseForAtMostOnce == base).
                    collect(Collectors.toUnmodifiableSet());
            if (duplicates.size() != 1)
                throw new RuntimeException("Duplicate " + base.getSimpleName() + " found: " + duplicates);
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
