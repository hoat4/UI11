package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

    static ResolutionRequestCollection combine(Map<ResolutionRequestCollection, Long> requestCollections) {
        Map<@Nullable Class<? extends ExposeRequest<?>>, Set<ResolutionRequest<?>>> reqsByBase = new HashMap<>();
        Map<ResolutionRequest<?>, Long> times = new HashMap<>();
        requestCollections.forEach((reqColl, time) -> {
            for (ResolutionRequest<?> req : reqColl.requests) {
                reqsByBase.computeIfAbsent(req.metadata().baseForAtMostOnce,
                        __ -> new HashSet<>()).add(req);
                if (times.put(req, time) != null)
                    throw new RuntimeException("TODO Req appears in multiple coll");
            }
        });
        Set<ResolutionRequest<?>> combined = new HashSet<>(reqsByBase.remove(null));
        reqsByBase.forEach((base, reqs) -> {
            List<ResolutionRequest<?>> sorted = reqs.stream().sorted(Comparator.comparingLong(times::get)).toList();
            if (sorted.size() > 1 && times.get(sorted.getFirst()).equals(times.get(sorted.get(1))))
                throw new RuntimeException("Can't determine order for " + sorted);
            combined.add(sorted.getFirst());
        });
        //System.out.println("Combine " + requestCollections + " to " + combined.stream().map(ResolutionRequest::toString).collect(joining("\n")));
        return new ResolutionRequestCollection(combined);
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
