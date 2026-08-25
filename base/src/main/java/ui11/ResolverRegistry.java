package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.function.Function;

public final class ResolverRegistry {

    private final List<Resolver<?>> resolvers = new ArrayList<>();

    ResolverRegistry() {
    }

    @NullMarked
    public <W extends SubstitutedWidget> Resolver<W> add(
            Priority priority,
            Class<W> widgetType,
            Function<W, Widget> f) {
        Resolver<W> r = new Resolver<>(priority, widgetType, f);
        resolvers.add(r);
        return r;
    }

    List<Resolver<?>> debug_allResolvers() {
        return resolvers;
    }

    List<Resolver<?>> findResolvers(SubstitutedWidget thiz, Set<? extends ResolutionRequest<?>> requests) {
        return resolvers.stream().
                filter(r -> r.widgetType.isInstance(thiz) &&
                        r.offers.stream().
                                allMatch(offer -> requests.stream().anyMatch(
                                        req -> req.requestData.peerType() == offer))).
                sorted(Comparator.comparingInt(r -> r.priority.ordinal())).
                toList();
    }


    public enum Priority {
        APPLICATION,
        NATIVE,
        EMULATED_BY_NATIVE,
        EMULATED,
        THEME
    }

    public final class Resolver<W extends SubstitutedWidget> {

        private final @NonNull Priority priority;
        @NonNull private final Class<W> widgetType;

        // most egyelőre elég ha ezek ending widgeteket tartalmazhatnak csak
        private final List<Class<? extends SubstitutedWidget>> offers = new ArrayList<>();
        private final List<Class<? extends SubstitutedWidget>> consumes = new ArrayList<>();
        private final @NonNull Function<@NonNull W, @NonNull Widget> f;

        private Resolver(@NonNull Priority priority,
                         @NonNull Class<W> widgetType,
                         @NonNull Function<@NonNull W, @NonNull Widget> f) {
            this.priority = Objects.requireNonNull(priority);
            this.widgetType = Objects.requireNonNull(widgetType);
            this.f = Objects.requireNonNull(f);
            widgetType.asSubclass(SubstitutedWidget.class);
            if (widgetType == SubstitutedWidget.class)
                throw new IllegalArgumentException("Widget type too broad, must be a subtype of widget");
        }

        // TODO itt a subtypeok mit jelentenek?
        @SafeVarargs
        public final Resolver<W> offers(@NonNull Class<? extends SubstitutedWidget>... widgets) {
            offers.addAll(List.of(widgets)); // implicit null check
            return this;
        }

        @SafeVarargs
        public final Resolver<W> consumes(@NonNull Class<? extends SubstitutedWidget>... widgets) {
            consumes.addAll(List.of(widgets)); // implicit null check
            return this;
        }

        @SuppressWarnings("unchecked")
        Widget invokeUnchecked(SubstitutedWidget widget) {
            return f.apply((W) widget); // TODO exceptionök
        }

        @Override
        public String toString() {
            return "Resolver for " + widgetType.getName() + ": " + f;
        }
    }
}
