package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ResolverRegistry {

    private final List<Transformer<?>> transformers = new ArrayList<>();
    private final List<PeerIndependentResolver<?>> peerIndependentResolvers = new ArrayList<>();
    private final List<PeerDependentResolver<?, ?>> peerDependentResolvers = new ArrayList<>();

    ResolverRegistry() {
    }

    public <SW extends SubstitutedWidget> void addPeerIndependent(
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerIndependentResolvers.add(new PeerIndependentResolver<>(null, widgetType, f));
    }

    public <SW extends SubstitutedWidget> void addPeerIndependentWithFilter(
            @NonNull Class<? extends PeerRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        validateRequestType(requestType);
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerIndependentResolvers.add(new PeerIndependentResolver<>(requestType, widgetType, f));
    }

    public <SW extends SubstitutedWidget, REQ extends PeerRequest<?>> void addPeerDependent(
            @NonNull Class<REQ> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull BiFunction<@NonNull SW, @NonNull REQ, @NonNull Widget> f) {
        validateRequestType(requestType);
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerDependentResolvers.add(new PeerDependentResolver<>(requestType, widgetType, f));
    }

    public <REQ extends PeerRequest<?>> void addPeerDependent(
            @NonNull Class<REQ> requestType,
            @NonNull Set<Class<? extends SubstitutedWidget>> widgetTypes,
            @NonNull Function<REQ, Widget> f) {
        // majd később talán lehet használni optimalizálásokra azt az infót, hogy ezeknek a widgettypeoknak
        // ugyanaz a resolverjük és nem is függnek a widgettől, tehát összevonhatóak.
        // De mivel egyelőre nincsenek optimalizálások, ezért csak egymástól függetlenül bejegyezzük.

        widgetTypes = Set.copyOf(widgetTypes);

        if (widgetTypes.isEmpty()) {
            validateRequestType(requestType);
            Objects.requireNonNull(f);
        } else {
            for (Class<? extends SubstitutedWidget> widgetType : widgetTypes)
                addPeerDependent(requestType, widgetType, (w, req) -> f.apply(req));
        }
    }

    public <SW extends SubstitutedWidget> void addTransformer(
            @NonNull Class<SW> widgetType,
            @NonNull BiFunction<@NonNull SW, @NonNull PeerRequest<Widget>, @NonNull Widget> f) {
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        transformers.add(new Transformer<>(widgetType, f));
    }

    private static void validateRequestType(Class<? extends PeerRequest<?>> requestType) {
        Objects.requireNonNull(requestType);
        requestType.asSubclass(PeerRequest.class);
        // TODO IntelliJ bug, enélkül a cast nélkül nem jelzi hibának
        if (requestType == (Class<? extends PeerRequest<?>>) (Class<?>) PeerRequest.class)
            throw new IllegalArgumentException();
    }

    private static void validateSubstitutedWidgetType(Class<? extends SubstitutedWidget> substitutedWidgetType) {
        Objects.requireNonNull(substitutedWidgetType);
        substitutedWidgetType.asSubclass(SubstitutedWidget.class);
        if (substitutedWidgetType == SubstitutedWidget.class)
            throw new IllegalArgumentException();
    }

    void findPeerDependentResolvers(
            SubstitutedWidget w,
            PeerRequest<?> req,
            Consumer<BiFunction<? extends SubstitutedWidget, ? extends PeerRequest<?>, Widget>> consumer) {
        for (PeerDependentResolver<?, ?> e : peerDependentResolvers) {
            if (!e.widgetType.isInstance(w))
                continue;
            if (!e.requestType.isInstance(req))
                continue;
            consumer.accept(e.f);
        }
    }

    void findPeerIndependentResolvers(
            SubstitutedWidget w,
            Set<? extends PeerRequest<?>> reqs,
            BiConsumer<Object /* key */, Function<? extends SubstitutedWidget, Widget>> consumer) {
        for (PeerIndependentResolver<?> e : peerIndependentResolvers) {
            if (!e.widgetType.isInstance(w))
                continue;
            if (e.requestType != null && reqs.stream().noneMatch(e.requestType::isInstance))
                continue;
            consumer.accept(e, e.f);
        }
    }

    List<Transformer<?>> findTransformers(SubstitutedWidget w) {
        List<Transformer<?>> l = new ArrayList<>();
        for (Transformer<?> e : transformers) {
            if (!e.widgetType.isInstance(w))
                continue;
            l.add(e);
        }
        return l;
    }

    Stream<Resolver> all() {
        return Stream.concat(peerDependentResolvers.stream(), peerIndependentResolvers.stream());
    }

    static sealed abstract class Resolver {

        final Class<? extends PeerRequest<?>> requestType; // csak peer independent esetén nullable
        final @NonNull Class<? extends SubstitutedWidget> widgetType;

        public Resolver(Class<? extends PeerRequest<?>> requestType,
                        @NonNull Class<? extends SubstitutedWidget> widgetType) {
            this.requestType = requestType;
            this.widgetType = widgetType;
        }
    }

    static final class PeerIndependentResolver<SW extends SubstitutedWidget> extends Resolver {

        private final Function<SW, Widget> f;

        public PeerIndependentResolver(@Nullable Class<? extends PeerRequest<?>> requestType,
                                       @NonNull Class<SW> widgetType,
                                       @NonNull Function<@NonNull SW, @NonNull Widget> f) {
            super(requestType, widgetType);
            this.f = f;
        }

        @Override
        public String toString() {
            return ReflectionUtil.simpleName(widgetType) + " " +
                    (requestType != null ? ReflectionUtil.simpleName(requestType) : "<any request>") +
                    " " + f;
        }
    }

    static final class PeerDependentResolver<SW extends SubstitutedWidget, REQ extends PeerRequest<?>> extends Resolver {

        private final BiFunction<SW, REQ, Widget> f;

        public PeerDependentResolver(@NonNull Class<REQ> requestType,
                                     @NonNull Class<SW> widgetType,
                                     @NonNull BiFunction<@NonNull SW, @NonNull REQ, @NonNull Widget> f) {
            super(requestType, widgetType);
            this.f = f;
        }

        @Override
        public String toString() {
            return ReflectionUtil.simpleName(widgetType) + " " +
                    ReflectionUtil.simpleName(requestType) +
                    " " + f;
        }
    }

    static final class Transformer<SW extends SubstitutedWidget> {

        final @NonNull Class<? extends SubstitutedWidget> widgetType;
        final @NonNull BiFunction<@NonNull SW, @NonNull PeerRequest<Widget>, @NonNull Widget> f;

        Transformer(@NonNull Class<? extends SubstitutedWidget> widgetType,
                    @NonNull BiFunction<@NonNull SW, @NonNull PeerRequest<Widget>, @NonNull Widget> f) {
            this.widgetType = widgetType;
            this.f = f;
        }

        @Override
        public String toString() {
            return "Transformer for " + ReflectionUtil.simpleName(widgetType) + ": " + f;
        }
    }

    static final class TransformerResultRequest extends PeerRequest<Widget> {

        static final TransformerResultRequest INSTANCE = new TransformerResultRequest();

        private TransformerResultRequest() {
            super(Widget.class);
        }
    }
}