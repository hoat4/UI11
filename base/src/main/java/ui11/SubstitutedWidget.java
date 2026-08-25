package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

// időnként felmerül, hogy ezt jó lenne külön package-be vinni, ResolverProvider és ResolverRegistry mellé.
// Először azért volt problémás, mert használta findInheritedValueForInjection-t egyrészt a WidgetResolver
// lookupolásához, másrészt a ResolutionContext::inherited implementálásához. ez megoldódott, át is került
// ui11.resolution package-be.
// De aztán jött hogy legyen mint SubstitutedWidget upvalue is egyben, meg az egész PeerRequest dolog,
// és így meg rengeteg hivatkozás lenne package-ek között, ezért inkább visszraktam a root package-be.

/**
 * A stateless widget which does not determine its content by itself, instead {@linkplain ResolverRegistry resolvers}
 * are asked for which content to display in it.
 * <p>
 * A {@linkplain SubstitutedWidget} must not contain fields annotated with {@link ui11.Widget.Inject @Inject} or
 * {@link Widget.Remember @Remember}.
 */
public abstract class SubstitutedWidget extends Widget {

    @Inject private ResolutionRequestCollection peerCreationRequestCollection;

    /**
     * Creates a new instance of SubstitutedWidget.
     */
    protected SubstitutedWidget() {
    }

    // TODO név
    protected SubstitutedWidget forSubstitution() {
        return this;
    }

    /**
     * It is final because there are no {@linkplain Remember state fields} permitted in the subclasses, so it is
     * not sensible for the subclasses to do anything in this method.
     * <p>
     * The implementation in this class also does nothing.
     */
    @Override
    protected final void initState() {
    }

    /**
     * It is final because there are no {@linkplain Remember state fields} permitted in the subclasses, so it is
     * not sensible for the subclasses to do anything in this method.
     * <p>
     * The implementation in this class also does nothing.
     */
    @Override
    protected final void onResume() {
    }

    /**
     * Asks the available {@linkplain ResolverProvider resolvers} to create a peer for each
     * {@linkplain PeerRequest request}. This method is not intended to be called by an application,
     * instead it will be called by the {@linkplain WidgetTree widget tree refresher} as any other widget.
     */
    @Override
    protected final Widget build() {
        SubstitutedWidget thiz = forSubstitution();
        Objects.requireNonNull(thiz, "SW.cFS");

    /*
        ResolverRegistry resolverRegistry = widgetState().tree.resolverRegistry;
        List<ResolverRegistry.Transformer<?>> transformers = resolverRegistry.findTransformers(thiz);

        return transformOrResolve(thiz, transformers, 0);
    }

    private Widget transformOrResolve(SubstitutedWidget thiz,
                                      List<ResolverRegistry.Transformer<?>> transformers,
                                      int i) {
        if (i == transformers.size())
            return doResolution(thiz);

        Widget w = invokeTransformer(transformers.get(i), thiz);
        return PeerRequest.requestSingle(w, ResolverRegistry.TransformerResultRequest.INSTANCE, result -> {
            Objects.requireNonNull(result);
            if (result.getClass() != getClass())
                // ez normális eset, csak ki kell találni, hogy hogyan kéne detektálni az ide-oda transzformálgatást
                throw new RuntimeException("TODO transformation resulted in different type: " +
                        result.getClass().getName() + " vs " + getClass().getName() + "\n" +
                        "Transformers: " + transformers);

            SubstitutedWidget thiz2 = (SubstitutedWidget) result;
            // itt már nem kell talán forSubstitution(), mert a keyek már assignolva lettek az eredetibe
            return transformOrResolve(thiz2, transformers, i + 1);
        });
    }

    @SuppressWarnings("unchecked")
    private Widget invokeTransformer(ResolverRegistry.Transformer<?> t,
                                     SubstitutedWidget thiz) {
        // TODO exceptionök?
        return ((BiFunction<SubstitutedWidget, PeerRequest<Widget>, Widget>) t.f).
                apply(thiz, ResolverRegistry.TransformerResultRequest.INSTANCE);
    }


    private Widget doResolution(SubstitutedWidget thiz) {
     */

        Set<? extends ResolutionRequest<?>> allRemainingRequests = new LinkedHashSet<>(peerCreationRequestCollection.requests());
        allRemainingRequests.removeIf(req -> {
            if (req.requestData.peerType().isInstance(thiz)) {
                req.setResult(thiz);
                return true;
            } else
                return false;
        });

        if (allRemainingRequests.isEmpty()) { // ha nem tűnt el az összes, akkor mi legyen?
            return new WidgetTree.ChainEnd();
        }

        ResolverRegistry resolverRegistry = widgetState().tree.resolverRegistry;
        List<ResolverRegistry.Resolver<?>> filteredResolvers = resolverRegistry.findResolvers(thiz, allRemainingRequests);

        if (filteredResolvers.size() > 1)
            throw new RuntimeException("Multiple resolvers available for widget " + thiz.getClass().getName() + ":\n- " +
                    filteredResolvers.stream().map(ResolverRegistry.Resolver::toString).collect(joining("\n- ")));

        if (filteredResolvers.isEmpty())
            throw cantFindResolver(allRemainingRequests, thiz);

        ResolverRegistry.Resolver<?> resolver = filteredResolvers.getFirst();
        return resolver.invokeUnchecked(thiz);
    }

    private @NonNull RuntimeException cantFindResolver(
            Set<? extends ResolutionRequest<?>> remainingReqs, SubstitutedWidget thiz) {
        StringBuilder s = new StringBuilder();
        s.append("No resolver supports and fallbackContent is not overriden on ");
        s.append(thiz.toString());
        s.append(": ").append(remainingReqs).append("\n").append("Refresh stack:");
        s.append(widgetState().tree.refreshStackToString());
        s.append("\nAvailable resolvers: ");
        widgetState().tree.resolverRegistry.debug_allResolvers().forEach(entry -> {
            s.append("\n- ").append(entry);
        });
        return new RuntimeException(s.toString());
    }

    /**
     * Subclasses can override this to provide a fallback widget which will be used if the available
     * {@linkplain ResolverProvider ResolverProviders} can't provide a resolver.
     * <p>
     * If not overridden, it always returns {@code null}.
     */
    // azért nullable és nem ez dobja az exceptiont hanem build, mert így nem csak típusonként lehet eldönteni
    // hogy kell-e fallbackContent, hanem az input mezők értékei alapján is dönthet úgy a subclass
    // hogy tud fallback contentet vagy nem.
    // bár ezt meg lehetne csinálni úgy is, hogy super.fallbackContent()-et hívják, ha nem tudnak fallbacket adni.
    protected @Nullable Widget fallbackContent() {
        return null;
    }

    // toString Widgetből van kezelve

    private static class NoPeerFactoryAvailable extends Widget {

        private final Class<? extends SubstitutedWidget> widgetType;
        private final ResolutionRequestCollection requests;

        NoPeerFactoryAvailable(Class<? extends SubstitutedWidget> widgetType,
                               ResolutionRequestCollection requests) {
            this.widgetType = widgetType;
            this.requests = requests;
        }

        @Override
        protected Widget build() {
            throw new RuntimeException("no " + ResolverProvider.class.getSimpleName() + " supports " +
                    widgetType.getName() + " and " + ReflectionUtil.simpleName(widgetType) +
                    ".fallbackContent() returned null\n" +
                    "Remaining requests: " + requests.requests() + "\n" +
                    "Refresh stack: " + widgetState().tree.refreshStackToString());
        }
    }

    private sealed interface ResolverWidgetKey {

        record OfGenericResolver(Object widgetResolver) implements ResolverWidgetKey {
        }

        // TODO ennek jobb key kéne
        record OfPeerSpecificResolver(PeerRequest<?> request) implements ResolverWidgetKey {
        }

        record OfFallback() implements ResolverWidgetKey {
        }
    }

}
