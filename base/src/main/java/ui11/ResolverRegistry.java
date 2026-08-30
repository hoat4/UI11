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

import static java.util.stream.Collectors.joining;

/**
 * Can be used to control the process of lowering the {@linkplain SubstitutedWidget SubstitutedWidgets} to
 * more concrete widgets.
 */
public final class ResolverRegistry {

    private final List<Transformer<?>> transformers = new ArrayList<>();
    private final List<PeerIndependentResolver<?>> peerIndependentResolvers = new ArrayList<>();
    private final List<PeerDependentResolver<?, ?>> peerDependentResolvers = new ArrayList<>();

    ResolverRegistry() {
    }

    <W extends SubstitutedWidget> void add(ResolverProvider.ResolutionRule<W> rule) {
        PeerIndependentResolver<W> r = new PeerIndependentResolver<>(
                rule.supportedRequestTypes, rule.widgetType, rule.f);
        peerIndependentResolvers.add(r);
    }

    /**
     * Adds a new resolver to this resolver registry.
     * It will be applicable if the widget is an instance of the specified type or its subtypes.
     * The widget will be passed to the function, and the widget returned from the function will replace
     * the original widget.
     */
    public <SW extends SubstitutedWidget> void add(
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerIndependentResolvers.add(new PeerIndependentResolver<>(Set.of(), widgetType, f));
    }

    public <SW extends SubstitutedWidget> void addPeerIndependentWithFilter(
            @NonNull Class<? extends PeerRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        validateRequestType(requestType);
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        peerIndependentResolvers.add(new PeerIndependentResolver<>(Set.of(requestType), widgetType, f));
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
            if (!e.requestTypes.isEmpty() &&
                    e.requestTypes.stream().allMatch(supportedRequestType ->
                    reqs.stream().noneMatch(supportedRequestType::isInstance)))
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

        final @NonNull Class<? extends SubstitutedWidget> widgetType;

        public Resolver(@NonNull Class<? extends SubstitutedWidget> widgetType) {
            this.widgetType = widgetType;
        }
    }

    static final class PeerIndependentResolver<SW extends SubstitutedWidget> extends Resolver {

        private final Function<SW, Widget> f;
        // TODO ez most kicsit zavaros, mert az üres set azt jelenti hogy minden requestet elfogad
        final Set<Class<? extends PeerRequest<?>>> requestTypes;

        public PeerIndependentResolver(@NonNull Set<Class<? extends PeerRequest<?>>> requestTypes,
                                       @NonNull Class<SW> widgetType,
                                       @NonNull Function<@NonNull SW, @NonNull Widget> f) {
            super(widgetType);
            this.requestTypes = requestTypes;
            this.f = f;
        }

        @Override
        public String toString() {
            return ReflectionUtil.simpleName(widgetType) + " " +
                    (requestTypes.isEmpty() ? "<any request>" : requestTypes.stream().
                            map(ReflectionUtil::simpleName).collect(joining(", ", "[", "]"))) +
                    " " + f;
        }
    }

    static final class PeerDependentResolver<SW extends SubstitutedWidget, REQ extends PeerRequest<?>> extends Resolver {

        private final BiFunction<SW, REQ, Widget> f;
        final Class<? extends PeerRequest<?>> requestType;

        public PeerDependentResolver(@NonNull Class<REQ> requestType,
                                     @NonNull Class<SW> widgetType,
                                     @NonNull BiFunction<@NonNull SW, @NonNull REQ, @NonNull Widget> f) {
            super(widgetType);
            this.requestType = requestType;
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