package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

// parentet azért kell, mert ezt használjuk akkor WidgetState.parents listában is,
// hogy lehessen ismerni IV-ket RefreshStack.pushIVs-ben.
// Az eredeti elképzelés az lett volna, hogy RefreshStack.ivs-t másoljuk le,
// de nem jó, mert annak csak az RRW alatti része kell, ráadásul ezt RRW-nként külön kéne tárolni.

/**
 * @param parent akkor null, ha a gyökér a {@linkplain #child()}
 * @param child
 * @param directIVs
 */
record WidgetInstantiation(
        @Nullable WidgetState<?> parent,
        @NonNull WidgetState<?> child,
        @NonNull Map<@NonNull Class<?>, @Nullable Object> directIVs,
        @Nullable ResolutionRequest<?> directReq,
        @NonNull Set<ResolutionRequest<?>> directCompletedRequests) {
    WidgetInstantiation {
        Objects.requireNonNull(child);
        Objects.requireNonNull(directIVs);
    }
}
