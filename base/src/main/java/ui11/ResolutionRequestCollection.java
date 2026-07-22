package ui11;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

final class ResolutionRequestCollection {

    /**
     * key: {@linkplain PeerCreationRequest#peerType() peer type}
     */
    public final @NonNull Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests;
    private final @NonNull Set<ResolutionRequest<?>> completed;

    private ResolutionRequestCollection(
            @NonNull Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests,
            @NonNull Set<ResolutionRequest<?>> completed) {
        this.requests = requests;
        this.completed = completed;

        assert requests.values().containsAll(completed);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + requests.toString();
    }

    public Collection<? extends ResolutionRequest<?>> remainingRequests() {
        Set<ResolutionRequest<?>> remaining = new HashSet<>(requests.values());
        remaining.removeAll(completed);
        return remaining;
    }

    public Collection<? extends ResolutionRequest<?>> completedRequests() {
        return completed;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        ResolutionRequestCollection that = (ResolutionRequestCollection) o;
        return requests.equals(that.requests) && completed.equals(that.completed);
    }

    @Override
    public int hashCode() {
        int result = requests.hashCode();
        result = 31 * result + completed.hashCode();
        return result;
    }

    static Builder builder() {
        return new Builder();
    }

    public List<ResolutionRequest<?>> byType(Class<? extends PeerCreationRequest<?>> type) {
        return requests.values().stream().
                filter(r -> type.isInstance(r.requestData) && !completed.contains(r)).
                toList();
    }

    static class Builder {

        private final @NonNull Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests = new HashMap<>();
        private final @NonNull Set<ResolutionRequest<?>> completed = new HashSet<>();

        private Builder() {
        }

        public int requestCount() {
            return requests.size();
        }

        public int completedRequestCount() {
            return completed.size();
        }

        public void addReq(ResolutionRequest<?> req) {
            if (requests.putIfAbsent(req.requestData.peerType(), req) != null)
                throw new RuntimeException("Multiple requests with peer type " + req.requestData.peerType());
        }

        public void addCompletions(@NonNull Set<? extends ResolutionRequest<?>> completions) {
            for (ResolutionRequest<?> completion : completions) {
                boolean added = completed.add(completion);
                assert added;
            }
        }

        public void addInherited(ResolutionRequestCollection reqColl) {
            reqColl.requests.values().forEach(this::addReq);
            addCompletions(reqColl.completed);
        }

        public ResolutionRequestCollection build() {
            return new ResolutionRequestCollection(requests, completed);
        }
    }
}
