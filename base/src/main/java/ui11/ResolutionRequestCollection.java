package ui11;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.stream.Collectors;

final class ResolutionRequestCollection {

    /**
     * key: {@linkplain PeerCreationRequest#peerType() peer type}
     */
    public final @NonNull Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests;
    private final @NonNull Set<Class<? extends SubstitutedWidget>> completed;

    private ResolutionRequestCollection(
            @NonNull Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> requests,
            @NonNull Set<Class<? extends SubstitutedWidget>> completed) {
        this.requests = requests;
        this.completed = completed;

        assert requests.keySet().containsAll(completed);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + requests.toString();
    }

    public Collection<? extends ResolutionRequest<?>> remainingRequests() {
        Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> m = new HashMap<>(requests);
        m.keySet().removeAll(completed);
        return m.values();
    }

    public Collection<? extends ResolutionRequest<?>> completedRequests() {
        Map<Class<? extends SubstitutedWidget>, ResolutionRequest<?>> m = new HashMap<>(requests);
        m.keySet().retainAll(completed);
        return m.values();
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
            addCompletions(reqColl.completed.stream().
                    map(reqColl.requests::get).collect(Collectors.toUnmodifiableSet()));
        }

        public ResolutionRequestCollection build() {
            Set<Class<? extends SubstitutedWidget>> completed2 = new HashSet<>();
            for (ResolutionRequest<?> completion : this.completed) {
                Class<? extends SubstitutedWidget> peerType = completion.requestData.peerType();
                assert requests.get(peerType) == completion;
                boolean added = completed2.add(peerType);
                assert added;
            }
            return new ResolutionRequestCollection(requests, completed2);
        }
    }
}
