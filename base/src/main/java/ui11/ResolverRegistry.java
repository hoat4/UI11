package ui11;

import org.jspecify.annotations.NonNull;
import ui11.reflectutil.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Can be used to control the process of lowering the {@linkplain SubstitutedWidget SubstitutedWidgets} to
 * more concrete widgets.
 */
public final class ResolverRegistry {

    final List<ResolutionRule<?>> eagerRules = new ArrayList<>();
    final List<ResolutionRule<?>> greedyRules = new ArrayList<>();
    private final List<Transformer<?>> transformers = new ArrayList<>();

    ResolverRegistry() {
    }

    /**
     * Adds a new resolver to this resolver registry.
     * It will be applicable if the widget is an instance of the specified type or its subtypes.
     * The widget will be passed to the function, and the widget returned from the function will replace
     * the original widget.
     */
    public <SW extends SubstitutedWidget> void register(
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        register(new ResolutionRule<>(widgetType, f, Set.of(), false));
    }

    public <SW extends SubstitutedWidget> void registerForContextType(
            @NonNull Class<? extends PeerRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        register(new ResolutionRule<>(widgetType, f, Set.of(requestType), false));
    }

    public <SW extends SubstitutedWidget> void registerForContextTypes(
            @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        if (supportedRequestTypes.isEmpty())
            throw new IllegalArgumentException("Supported request types is empty");
        register(new ResolutionRule<>(widgetType, f, supportedRequestTypes, false));
    }

    public <SW extends SubstitutedWidget> void registerPeerResolver(
            @NonNull Class<? extends PeerRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        register(new ResolutionRule<>(widgetType, f, Set.of(requestType), true));
    }

    public <SW extends SubstitutedWidget> void registerPeerResolver(
            @NonNull Set<Class<? extends PeerRequest<?>>> requestTypes,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        if (requestTypes.isEmpty())
            throw new IllegalArgumentException("Supported request types is empty");
        register(new ResolutionRule<>(widgetType, f, requestTypes, true));
    }

    private <W extends SubstitutedWidget> void register(ResolutionRule<W> rule) {
        if (rule.coexistWithOtherResolvers)
            eagerRules.add(rule);
        else
            greedyRules.add(rule);
    }

    public <SW extends SubstitutedWidget> void registerTransformer(
            @NonNull Class<SW> widgetType,
            @NonNull BiFunction<@NonNull SW, @NonNull UnaryOperator<Widget>, @NonNull Widget> f) {
        validateSubstitutedWidgetType(widgetType);
        Objects.requireNonNull(f);
        transformers.add(new Transformer<>(widgetType, f));
    }

    private static void validateSubstitutedWidgetType(Class<? extends SubstitutedWidget> substitutedWidgetType) {
        Objects.requireNonNull(substitutedWidgetType);
        substitutedWidgetType.asSubclass(SubstitutedWidget.class);
        if (substitutedWidgetType == SubstitutedWidget.class)
            throw new IllegalArgumentException();
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

    Stream<ResolutionRule<?>> debug_allResolvers() {
        return greedyRules.stream();
    }

    // ennek identity equalsjére SubstitutedWidgetben a keyek dependelnek
    static final class ResolutionRule<W extends SubstitutedWidget> {

        final @NonNull Class<W> widgetType;
        final @NonNull Function<W, Widget> f;
        // TODO ez most kicsit zavaros, mert az üres set azt jelenti hogy minden requestet elfogad
        final @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes;
        /**
         * ha ez true, akkor {@link #supportedRequestTypes} nem üres
         */
        final boolean coexistWithOtherResolvers;

        /**
         * @param supportedRequestTypes a hívónak kell szükség esetén ellenőriznie, hogy ez üres-e
         */
        public ResolutionRule(@NonNull Class<W> widgetType,
                              @NonNull Function<W, Widget> f,
                              @NonNull Set<Class<? extends PeerRequest<?>>> supportedRequestTypes,
                              boolean isPeerResolver) {
            this.widgetType = Objects.requireNonNull(widgetType);
            widgetType.asSubclass(SubstitutedWidget.class);
            if (widgetType == SubstitutedWidget.class)
                throw new IllegalArgumentException();
            this.f = Objects.requireNonNull(f);

            Set<Class<? extends PeerRequest<?>>> set = Set.copyOf(supportedRequestTypes);
            set.forEach(t -> {
                if (t.asSubclass(PeerRequest.class) == PeerRequest.class)
                    throw new IllegalArgumentException();
            });
            this.supportedRequestTypes = set;

            this.coexistWithOtherResolvers = isPeerResolver;
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

    static final class Transformer<SW extends SubstitutedWidget> {

        final @NonNull Class<? extends SubstitutedWidget> widgetType;
        final @NonNull BiFunction<@NonNull SW, @NonNull UnaryOperator<Widget>, @NonNull Widget> f;

        Transformer(@NonNull Class<? extends SubstitutedWidget> widgetType,
                    @NonNull BiFunction<@NonNull SW, @NonNull UnaryOperator<Widget>, @NonNull Widget> f) {
            this.widgetType = widgetType;
            this.f = f;
        }

        @SuppressWarnings("unchecked")
        Widget invokeUnchecked(SubstitutedWidget thiz,
                               UnaryOperator<Widget> nextInChain) {
            // TODO exceptionök?
            return ((BiFunction<SubstitutedWidget, UnaryOperator<Widget>, Widget>) f).
                    apply(thiz, nextInChain);
        }

        @Override
        public String toString() {
            return "Transformer for " + ReflectionUtil.simpleName(widgetType) + ": " + f;
        }
    }
}