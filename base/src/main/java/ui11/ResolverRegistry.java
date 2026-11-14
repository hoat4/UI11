package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

// TODO kéne valami prioritás, mert így most nem lehet a classpathban layout-constraint ha platform-dom is ott van

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

    void add(ResolverRegistry r, ResolverProvider.Priority p) {
        for (ResolutionRule<?> rule : r.eagerRules)
            eagerRules.add(rule.withPriority(p));
        for (ResolutionRule<?> rule : r.greedyRules)
            greedyRules.add(rule.withPriority(p));
        transformers.addAll(r.transformers);
    }

    @SuppressWarnings("DataFlowIssue")
    void sortByPriority() {
        eagerRules.sort(Comparator.comparingInt(r -> r.priority.ordinal()));
        greedyRules.sort(Comparator.comparingInt(r -> r.priority.ordinal()));
    }

    /**
     * Adds a new resolver to this resolver registry.
     * It will be applicable if the widget is an instance of the specified type or its subtypes.
     * The widget will be passed to the function, and the widget returned from the function will replace
     * the original widget.
     */
    @Deprecated
    public <SW extends SubstitutedWidget> void register(
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        register(new ResolutionRule<>(widgetType, f, Set.of(), false, null));
    }

    @Deprecated
    public <SW extends SubstitutedWidget> void registerForContextType(
            @NonNull Class<? extends ExposeRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        register(new ResolutionRule<>(widgetType, f, Set.of(requestType), false, null));
    }

    @Deprecated
    public <SW extends SubstitutedWidget> void registerForContextTypes(
            @NonNull Set<Class<? extends ExposeRequest<?>>> supportedRequestTypes,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        if (supportedRequestTypes.isEmpty())
            throw new IllegalArgumentException("Supported request types is empty");
        register(new ResolutionRule<>(widgetType, f, supportedRequestTypes, false, null));
    }

    @Deprecated
    public <SW extends SubstitutedWidget> void registerPeerResolver(
            @NonNull Class<? extends ExposeRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        register(new ResolutionRule<>(widgetType, f, Set.of(requestType), true, null));
    }

    @Deprecated
    public <SW extends SubstitutedWidget> void registerPeerResolver(
            @NonNull Set<Class<? extends ExposeRequest<?>>> requestTypes,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        if (requestTypes.isEmpty())
            throw new IllegalArgumentException("Supported request types is empty");
        register(new ResolutionRule<>(widgetType, f, requestTypes, true, null));
    }

    public <W extends SubstitutedWidget> void register(@NonNull ResolutionRule<W> rule) {
        Objects.requireNonNull(rule);
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
    public static final class ResolutionRule<W extends SubstitutedWidget> {

        final @NonNull Class<W> widgetType;
        final @NonNull Function<W, Widget> f;
        // TODO ez most kicsit zavaros, mert az üres set azt jelenti hogy minden requestet elfogad
        final @NonNull Set<Class<? extends ExposeRequest<?>>> supportedRequestTypes;
        /**
         * ha ez true, akkor {@link #supportedRequestTypes} nem üres
         */
        final boolean coexistWithOtherResolvers;
        /**
         * ResolverRegistry-k kombinálásakor kap értéket
         */
        final ResolverProvider.Priority priority;

        public ResolutionRule(@NonNull Class<W> widgetType,
                              @NonNull Function<@NonNull W, @NonNull Widget> f) {
            this(widgetType, f, Set.of(), false, null);
        }

        /**
         * @param supportedRequestTypes a hívónak kell szükség esetén ellenőriznie, hogy ez üres-e
         */
        ResolutionRule(@NonNull Class<W> widgetType,
                       @NonNull Function<W, Widget> f,
                       @NonNull Set<Class<? extends ExposeRequest<?>>> supportedRequestTypes,
                       boolean coexistWithOtherResolvers,
                       ResolverProvider.@Nullable Priority priority) {
            this.widgetType = Objects.requireNonNull(widgetType);
            widgetType.asSubclass(SubstitutedWidget.class);
            if (widgetType == SubstitutedWidget.class)
                throw new IllegalArgumentException();
            this.f = Objects.requireNonNull(f);
            this.priority = priority;

            Set<Class<? extends ExposeRequest<?>>> set = Set.copyOf(supportedRequestTypes);
            set.forEach(t -> {
                if (t.asSubclass(ExposeRequest.class) == ExposeRequest.class)
                    throw new IllegalArgumentException();
            });
            this.supportedRequestTypes = set;

            this.coexistWithOtherResolvers = coexistWithOtherResolvers;
        }

        @SafeVarargs
        public final ResolutionRule<W> onlyOnRequestTypes(@NonNull Class<? extends ExposeRequest<?>> @NonNull ... supportedRequestTypes) {
            return onlyOnRequestTypes(Set.of(supportedRequestTypes));
        }

        public ResolutionRule<W> onlyOnRequestTypes(@NonNull Set<Class<? extends ExposeRequest<?>>> supportedRequestTypes) {
            supportedRequestTypes = Set.copyOf(supportedRequestTypes);
            if (supportedRequestTypes.isEmpty())
                throw new IllegalArgumentException("supportedRequestTypes must not be empty");
            return new ResolutionRule<>(widgetType, f, supportedRequestTypes, coexistWithOtherResolvers, priority);
        }

        public ResolutionRule<W> parallelWithOtherResolvers() {
            return new ResolutionRule<>(widgetType, f, supportedRequestTypes, true, priority);
        }

        ResolutionRule<W> withPriority(ResolverProvider.Priority priority) {
            assert this.priority == null;
            return new ResolutionRule<>(widgetType, f, supportedRequestTypes, true, priority);
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