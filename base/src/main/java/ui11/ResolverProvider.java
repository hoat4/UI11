package ui11;

import org.jspecify.annotations.NonNull;
import ui11.reflectutil.ReflectionUtil;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.joining;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
// TODO multithreading specifikálása
public interface ResolverProvider {

    @Deprecated
    default void configure(ResolverRegistry r) {
        for (ResolutionRule<?> rule : rules())
            r.add(rule);
    }

    default List<ResolutionRule<?>> rules() {
        throw new UnsupportedOperationException();
    }

    final class ResolutionRule<W extends SubstitutedWidget> {

        final @NonNull Class<W> widgetType;
        final @NonNull Function<W, Widget> f;
        // TODO ez most kicsit zavaros, mert az üres set azt jelenti hogy minden requestet elfogad
        final @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes;

        public ResolutionRule(@NonNull Class<W> widgetType,
                              @NonNull Function<W, Widget> f) {
            this.widgetType = Objects.requireNonNull(widgetType);
            widgetType.asSubclass(SubstitutedWidget.class);
            if (widgetType == SubstitutedWidget.class)
                throw new IllegalArgumentException();
            this.f = Objects.requireNonNull(f);
            this.supportedRequestTypes = Set.of();
        }

        private ResolutionRule(ResolutionRule<W> old,
                               @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes) {
            this.widgetType = old.widgetType;
            this.f = old.f;
            this.supportedRequestTypes = supportedRequestTypes;
        }

        public ResolutionRule<W> requires(Class<? extends PeerRequest<?>> requestType) {
            return requiresEither(requestType);
        }

        @SafeVarargs
        public final ResolutionRule<W> requiresEither(Class<? extends PeerRequest<?>>... requestTypes) {
            Set<Class<? extends PeerRequest<?>>> set = Set.of(requestTypes);
            if (set.isEmpty())
                throw new IllegalArgumentException("No type specified to requiresEither");
            set.forEach(t -> {
                if (t.asSubclass(PeerRequest.class) == PeerRequest.class)
                    throw new IllegalArgumentException();
            });
            return new ResolutionRule<>(this, set);
        }

        @Override
        public String toString() {
            return ReflectionUtil.simpleName(widgetType) + " " +
                    (supportedRequestTypes.isEmpty() ? "<any request>" : supportedRequestTypes.stream().
                            map(ReflectionUtil::simpleName).collect(joining(", ", "[", "]"))) +
                    " " + f;
        }
    }
}
