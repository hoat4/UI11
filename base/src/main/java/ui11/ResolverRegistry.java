package ui11;

import org.jspecify.annotations.NonNull;
import ui11.ResolverProvider.ResolutionRule;
import ui11.reflectutil.ReflectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

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

    <W extends SubstitutedWidget> void add(ResolutionRule<W> rule) {
        if (rule.coexistWithOtherResolvers)
            eagerRules.add(rule);
        else
            greedyRules.add(rule);
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
        add(new ResolutionRule<>(widgetType, f));
    }

    public <SW extends SubstitutedWidget> void addPeerIndependentWithFilter(
            @NonNull Class<? extends PeerRequest<?>> requestType,
            @NonNull Class<SW> widgetType,
            @NonNull Function<@NonNull SW, Widget> f) {
        add(new ResolutionRule<>(widgetType, f).requires(requestType));
    }

    public <SW extends SubstitutedWidget> void addTransformer(
            @NonNull Class<SW> widgetType,
            @NonNull BiFunction<@NonNull SW, @NonNull PeerRequest<Widget>, @NonNull Widget> f) {
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