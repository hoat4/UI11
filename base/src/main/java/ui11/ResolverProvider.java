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

    // ennek identity equalsjére SubstitutedWidgetben a keyek dependelnek
    final class ResolutionRule<W extends SubstitutedWidget> {

        final @NonNull Class<W> widgetType;
        final @NonNull Function<W, Widget> f;
        // TODO ez most kicsit zavaros, mert az üres set azt jelenti hogy minden requestet elfogad
        final @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes;
        /**
         * ha ez true, akkor {@link #supportedRequestTypes} nem üres
         */
        final boolean coexistWithOtherResolvers;

        public ResolutionRule(@NonNull Class<W> widgetType,
                              @NonNull Function<W, Widget> f) {
            this.widgetType = Objects.requireNonNull(widgetType);
            widgetType.asSubclass(SubstitutedWidget.class);
            if (widgetType == SubstitutedWidget.class)
                throw new IllegalArgumentException();
            this.f = Objects.requireNonNull(f);
            this.supportedRequestTypes = Set.of();
            this.coexistWithOtherResolvers = false;
        }

        private ResolutionRule(ResolutionRule<W> old,
                               @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes,
                               boolean coexistWithOtherResolvers) {
            this.widgetType = old.widgetType;
            this.f = old.f;
            this.supportedRequestTypes = supportedRequestTypes;
            this.coexistWithOtherResolvers = coexistWithOtherResolvers;
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
            return new ResolutionRule<>(this, set, coexistWithOtherResolvers);
        }

        public ResolutionRule<W> coexistWithOtherResolvers() {
            if (supportedRequestTypes.isEmpty())
                throw new IllegalStateException("Can't coexist with other resolvers is " +
                        "supported request types is not specified");
            return new ResolutionRule<>(this, supportedRequestTypes, true);
        }

        boolean matches(SubstitutedWidget widget, Set<? extends ResolutionRequest<?>> reqs) {
            if (!widgetType.isInstance(widget))
                return false;
            if (supportedRequestTypes.isEmpty())
                return true;
            return supportedRequestTypes.stream().anyMatch(supportedRequestType ->
                    reqs.stream().anyMatch(req ->
                            supportedRequestType.isInstance(req.requestData)));
        }

        Widget invokeUnchecked(SubstitutedWidget widgetToBeSubstituted) {
            @SuppressWarnings("unchecked") Function<SubstitutedWidget, Widget> castedF =
                    (Function<SubstitutedWidget, Widget>) f;
            Widget w = castedF.apply(widgetToBeSubstituted); // TODO exceptionök
            if (w == null)
                throw new NullPointerException("Rule returned null: " + this); // TODO értelmesebb hibaüzenet
            return w;
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
