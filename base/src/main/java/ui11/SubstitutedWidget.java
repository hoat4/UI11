package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

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
     * <p>
     * If no peer could be created for a request, then it will be
     * forwarded to a {@linkplain ResolverRegistry#addPeerIndependentWithFilter(Class, Class, Function) peer independent resolver}.
     */
    @Override
    protected final Widget build() {
        SubstitutedWidget thiz = forSubstitution();
        Objects.requireNonNull(thiz, "SW.cFS");

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
        Set<? extends ResolutionRequest<?>> allRemainingRequests = new LinkedHashSet<>(peerCreationRequestCollection.requests());

        Map<ResolverWidgetKey, Widget> childrenWidgets = new HashMap<>();
        Map<ResolverWidgetKey, Map<PeerRequest<?>, Set<ResolutionRequest<?>>>> childrenReqs = new HashMap<>();

        ResolverRegistry resolverRegistry = widgetState().tree.resolverRegistry;

        // peer-specifikus resolvereket előbbre vesszük, mint a nem peer-specifikusakat,
        // mert ha peer-specifikusakkal ki elégíteni, akkor lehet hogy a nem peer-specifikus nem is kell
        Set<ResolutionRequest<?>> handledUsingPeerSpecificResolvers = new HashSet<>();
        for (ResolutionRequest<?> req : allRemainingRequests) {
            resolverRegistry.findPeerDependentResolvers(thiz, req.requestData, f -> {
                if (!handledUsingPeerSpecificResolvers.add(req))
                    throw new RuntimeException("Multiple resolvers has applicable " +
                            "tryResolveRequestSpecific for " + thiz + " and " + req + ": " + resolverRegistry);

                @SuppressWarnings("unchecked") BiFunction<SubstitutedWidget, PeerRequest<?>, Widget> castedF =
                        (BiFunction<SubstitutedWidget, PeerRequest<?>, Widget>) f;
                Widget w = castedF.apply(thiz, req.requestData); // TODO exceptionök
                if (w == null)
                    throw new NullPointerException(f + " returned null"); // TODO értelmesebb hibaüzenet

                ResolverWidgetKey.OfPeerSpecificResolver key =
                        new ResolverWidgetKey.OfPeerSpecificResolver(req);

                Object prev = childrenWidgets.putIfAbsent(key, w);
                assert prev == null;
                if (childrenReqs.put(key, new HashMap<>(Map.of(req.requestData, new HashSet<>(Set.of(req))))) != null)
                    throw new RuntimeException("Duplicate req key: " + key);
            });
        }

        Set<ResolutionRequest<?>> remainedAfterPeerSpecificResolvers =
                new HashSet<>(allRemainingRequests);
        remainedAfterPeerSpecificResolvers.removeAll(handledUsingPeerSpecificResolvers);

        // amelyiknek van default értéke, azt ne adjuk tovább generic resolvereknek.
        // mert ebből az lenne, hogy pl. WeightRequest miatt elkezdené a natív control helyett felépíteni
        // az emuláltat.
        int[] removedBecauseDefaultValue = {0};
        remainedAfterPeerSpecificResolvers.removeIf(req -> {
            Object defaultValue;
            if ((defaultValue = req.requestData.defaultValue()) != null) {
                req.setResult(defaultValue);
                removedBecauseDefaultValue[0]++;
                return true;
            } else
                return false;
        });

        Map<PeerRequest<?>, Set<ResolutionRequest<?>>> remainingForGeneric =
                remainedAfterPeerSpecificResolvers.stream().
                        collect(groupingBy(r -> r.requestData, toSet()));

        boolean[] foundGenericResolver = {false};

        // tryResolveGeneric-eket akkor is végrehajtjuk, ha minden req-t lefednek a peer-specifikusok, hogy
        // multiple resolvers applicable hibák kijöjjenek
        resolverRegistry.findPeerIndependentResolvers(thiz, remainingForGeneric.keySet(), (keyO, f) -> {
            if (foundGenericResolver[0])
                // TODO ezt már ResolverRegistrynek kéne detektálnia
                throw new RuntimeException("Multiple resolvers has applicable tryResolveGeneric for " +
                        thiz + ": " + resolverRegistry);
            foundGenericResolver[0] = true;

            @SuppressWarnings("unchecked") Function<SubstitutedWidget, Widget> castedF = (Function<SubstitutedWidget, Widget>) f;
            Widget w = castedF.apply(thiz); // TODO exceptionök
            if (w == null)
                throw new NullPointerException(f + " returned null"); // TODO értelmesebb hibaüzenet

            if (!remainingForGeneric.isEmpty()) {
                ResolverWidgetKey.OfGenericResolver key = new ResolverWidgetKey.OfGenericResolver(keyO);
                childrenWidgets.put(key, w);
                childrenReqs.put(key, remainingForGeneric);
            }
        });

        if (allRemainingRequests.size() != handledUsingPeerSpecificResolvers.size() + removedBecauseDefaultValue[0]) {
            // van olyan req, amit a peer-specifikusok nem fednek le
            if (!foundGenericResolver[0]) {
                Widget w = thiz.fallbackContent(); // TODO exceptionök
                if (w != null) {
                    ResolverWidgetKey.OfFallback key = new ResolverWidgetKey.OfFallback();
                    childrenWidgets.put(key, w);
                    childrenReqs.put(key, remainingForGeneric);
                } else {
                    throw cantFindResolver(remainedAfterPeerSpecificResolvers, thiz);
                }
            }
        }

        Map<ResolverWidgetKey, Set<PeerRequest<?>>> childrenReqs2 =
                childrenReqs.entrySet().stream().collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().keySet()));
        return PeerRequest.requestMultiple(childrenWidgets, childrenReqs2, results -> {
            results.forEach((req, resultsByKey) -> {
                resultsByKey.forEach((key, result) -> {
                    for (ResolutionRequest<?> parentResolutionRequest : childrenReqs.get(key).get(req))
                        parentResolutionRequest.setResult(result);
                });
            });
            return new WidgetTree.ChainEnd();
        });
    }

    private @NonNull RuntimeException cantFindResolver(
            Set<ResolutionRequest<?>> remainingReqs, SubstitutedWidget thiz) {
        StringBuilder s = new StringBuilder();
        s.append("No resolver supports and fallbackContent is not overriden on ");
        s.append(thiz.toString());
        s.append(": ").append(remainingReqs).append("\n").append("Refresh stack:");
        s.append(widgetState().tree.refreshStackToString());
        s.append("\nAvailable resolvers: ");
        widgetState().tree.resolverRegistry.all().forEach(entry -> {
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

        record OfPeerSpecificResolver(ResolutionRequest<?> request) implements ResolverWidgetKey {
        }

        record OfFallback() implements ResolverWidgetKey {
        }
    }

}
