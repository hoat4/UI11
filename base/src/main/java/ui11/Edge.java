package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

// parentet azért kell, mert ezt használjuk akkor Node.parents listában is,
// hogy lehessen ismerni IV-ket RefreshStack.pushIVs-ben.
// Az eredeti elképzelés az lett volna, hogy RefreshStack.ivs-t másoljuk le,
// de nem jó, mert annak csak az RRW alatti része kell, ráadásul ezt RRW-nként külön kéne tárolni.

/**
 *
 */
final class Edge {

    private final @Nullable Node<?> parent;
    private final @NonNull Node<?> child;
    private final @NonNull Map<@NonNull Class<?>, @Nullable Object> directIVs;
    private final long instantiatedAt;

    Edge(@Nullable Node<?> parent, @NonNull Node<?> child, @NonNull Map<@NonNull Class<?>, @Nullable Object> directIVs, long instantiatedAt) {
        Objects.requireNonNull(child);
        Objects.requireNonNull(directIVs);
        this.parent = parent;
        this.child = child;
        this.directIVs = directIVs;
        this.instantiatedAt = instantiatedAt;
    }

    public @Nullable Node<?> parent() {
        return parent;
    }

    public @NonNull Node<?> child() {
        return child;
    }

    public @NonNull Map<@NonNull Class<?>, @Nullable Object> directIVs() {
        return directIVs;
    }

    public long instantiatedAt() {
        return instantiatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Edge) obj;
        return Objects.equals(this.parent, that.parent) &&
            Objects.equals(this.child, that.child) &&
            Objects.equals(this.directIVs, that.directIVs) &&
            this.instantiatedAt == that.instantiatedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child, directIVs, instantiatedAt);
    }

    @Override
    public String toString() {
        return "Edge[" +
            "parent=" + parent + ", " +
            "child=" + child + ", " +
            "directIVs=" + directIVs + ", " +
            "instantiatedAt=" + instantiatedAt + ']';
    }
}
