package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

// időnként felmerül, hogy ezt jó lenne külön package-be vinni, WidgetResolver és GlobalWidgetResolvers mellé.
// Először azért volt problémás, mert használta findInheritedValueForInjection-t egyrészt a WidgetResolver
// lookupolásához, másrészt a ResolutionContext::inherited implementálásához. ez megoldódott, át is került
// ui11.resolution package-be.
// De aztán jött hogy legyen mint SubstitutedWidget upvalue is egyben, meg az egész PeerRequest dolog,
// és így meg rengeteg hivatkozás lenne package-ek között, ezért inkább visszraktam a root package-be.

/**
 * A stateless widget which does not determine its content by itself, instead a {@link WidgetResolver} is asked for
 * which content to display in it.
 * <p>
 * The following resolvers are used:
 * <ol>
 *     <li>Global resolvers: these can be defined as {@link ServiceLoader java.util.ServiceLoader} services,
 *         where the SPI interface is {@link WidgetResolver ui11.WidgetResolver}.
 *     <li>The value of {@linkplain ui11.provide.Provider inherited value} of type {@link WidgetResolver}.
 *         Only one can be inherited, but {@link WidgetResolver#composite(WidgetResolver, WidgetResolver)} can be used
 *         to combine multiple WidgetResolvers.
 * </ol>
 */
public abstract class SubstitutedWidget extends Widget {

    @Inject(required = false) private WidgetResolver widgetResolver;
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
    // TODO ez most ideiglenesen nem-final lett, Slot2 miatt
    @Override
    protected void initState() {
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
     * Asks the available {@linkplain WidgetResolver WidgetResolvers} to create a peer for each
     * {@linkplain ui11.PeerRequestor.Request request}. This method is not intended to be called by an application,
     * instead it will be called by the {@linkplain WidgetTree widget tree refresher} as any other widget.
     * <p>
     * If no peer could be created for a request, then it will be
     * forwarded to a {@linkplain WidgetResolver#tryResolveGeneric(SubstitutedWidget) generic peer}.
     */
    @Override
    protected final Widget build() {
        Set<? extends ResolutionRequest<?>> allRemainingRequests = new LinkedHashSet<>(peerCreationRequestCollection.requests());
        allRemainingRequests.removeIf(req -> {
            Object defaultValue;
            if ((defaultValue = req.requestData.defaultValue()) != null) {
                req.setResult(defaultValue);
                return true;
            } else
                return false;
        });

        SubstitutedWidget thiz = forSubstitution();
        Objects.requireNonNull(thiz, "SW.cFS");

        WidgetResolver.CompositeWidgetResolver resolvers =
                (widgetResolver != null ?
                        (WidgetResolver.CompositeWidgetResolver)
                                WidgetResolver.composite(GlobalWidgetResolvers.instance(), widgetResolver) :
                        GlobalWidgetResolvers.instance());

        Map<ResolverWidgetKey, Widget> childrenWidgets = new HashMap<>();
        Map<ResolverWidgetKey, Map<PeerRequestor.Request<?>, ResolutionRequest<?>>> childrenReqs = new HashMap<>();

        // peer-specifikus resolvereket előbbre vesszük, mint a nem peer-specifikusakat,
        // mert ha peer-specifikusakkal ki elégíteni, akkor lehet hogy a nem peer-specifikus nem is kell
        Set<ResolutionRequest<?>> handledUsingPeerSpecificResolvers = new HashSet<>();
        for (WidgetResolver resolver : resolvers.leaves()) {
            for (ResolutionRequest<?> req : allRemainingRequests) {
                Widget w = resolver.tryResolveRequestSpecific(thiz, req.requestData); // TODO exceptionök
                if (w != null) {
                    if (!handledUsingPeerSpecificResolvers.add(req))
                        throw new RuntimeException("Multiple resolvers has applicable " +
                                "tryResolveRequestSpecific for " + thiz + " and " + req + ": " + resolvers);

                    ResolverWidgetKey.OfPeerSpecificResolver key =
                            new ResolverWidgetKey.OfPeerSpecificResolver(req.requestData);
                    Object prev = childrenWidgets.putIfAbsent(key, w);
                    assert prev == null;
                    childrenReqs.put(key, Map.of(req.requestData, req));
                }
            }
        }

        Set<ResolutionRequest<?>> remainedAfterPeerSpecificResolvers =
                new HashSet<>(allRemainingRequests);
        remainedAfterPeerSpecificResolvers.removeAll(handledUsingPeerSpecificResolvers);
        Map<PeerRequestor.Request<?>, ResolutionRequest<?>> remainingForGeneric =
                remainedAfterPeerSpecificResolvers.stream().
                        collect(toMap(r -> r.requestData, r -> r));

        boolean foundGenericResolver = false;

        // tryResolveGeneric-eket akkor is végrehajtjuk, ha minden req-t lefednek a peer-specifikusok, hogy
        // multiple resolvers applicable hibák kijöjjenek
        for (WidgetResolver resolver : resolvers.leaves()) {
            Widget w = resolver.tryResolveGeneric(thiz); // TODO exceptionök
            if (w == null)
                continue;

            if (foundGenericResolver)
                throw new RuntimeException("Multiple resolvers has applicable tryResolveGeneric for " +
                        thiz + ": " + resolvers);
            foundGenericResolver = true;

            if (!remainingForGeneric.isEmpty()) {
                ResolverWidgetKey.OfGenericResolver key = new ResolverWidgetKey.OfGenericResolver(resolver);
                childrenWidgets.put(key, w);
                childrenReqs.put(key, remainingForGeneric);
            }
        }

        if (allRemainingRequests.size() != handledUsingPeerSpecificResolvers.size()) {
            // van olyan req, amit a peer-specifikusok nem fednek le
            if (!foundGenericResolver) {
                Widget w = thiz.fallbackContent(); // TODO exceptionök
                if (w != null) {
                    ResolverWidgetKey.OfFallback key = new ResolverWidgetKey.OfFallback();
                    childrenWidgets.put(key, w);
                    childrenReqs.put(key, remainingForGeneric);
                } else {
                    throw cantFindResolver(handledUsingPeerSpecificResolvers, thiz);
                }
            }
        }

        Map<ResolverWidgetKey, Set<PeerRequestor.Request<?>>> childrenReqs2 =
                childrenReqs.entrySet().stream().collect(toMap(Map.Entry::getKey,
                        e -> e.getValue().keySet()));
        return PeerRequestor.ofMultiple(childrenWidgets, childrenReqs2, results -> {
            results.forEach((req, resultsByKey) -> {
                resultsByKey.forEach((key, result) -> {
                    ResolutionRequest<?> parentResolutionRequest = childrenReqs.get(key).get(req);
                    assert parentResolutionRequest != null;
                    parentResolutionRequest.setResult(result);
                });
            });
            return new WidgetTree.ChainEnd();
        });
    }

    private @NonNull RuntimeException cantFindResolver(
            Set<ResolutionRequest<?>> handledUsingPeerSpecificResolvers, SubstitutedWidget thiz) {
        Set<ResolutionRequest<?>> remainingReqs = new HashSet<>(peerCreationRequestCollection.requests());
        remainingReqs.removeAll(handledUsingPeerSpecificResolvers);
        StringBuilder s = new StringBuilder();
        s.append("No resolver supports and fallbackContent is not overriden on ");
        s.append(thiz.toString());
        s.append(": ").append(remainingReqs).append("\n").append("Refresh stack:");
        s.append(widgetState().tree.refreshStackToString());
        s.append("\nAvailable resolvers: ");
        if (widgetResolver != null)
            if (widgetResolver instanceof WidgetResolver.CompositeWidgetResolver c)
                for (WidgetResolver r : c.leaves())
                    s.append("\n- ").append(r).append(" (from IV)");
            else
                s.append("\n- ").append(widgetResolver).append(" (from IV)");
        for (WidgetResolver r : GlobalWidgetResolvers.instance().leaves())
            s.append("\n- ").append(r).append(" (global)");
        return new RuntimeException(s.toString());
    }

    /**
     * Subclasses can override this to provide a fallback widget which will be used if the available
     * {@linkplain WidgetResolver WidgetResolvers} can't provide a peer.
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
            throw new RuntimeException("no " + WidgetResolver.class.getSimpleName() + " supports " +
                    widgetType.getName() + " and " + ReflectionUtil.simpleName(widgetType) +
                    ".fallbackContent() returned null\n" +
                    "Remaining requests: " + requests.requests() + "\n" +
                    "Refresh stack: " + widgetState().tree.refreshStackToString());
        }
    }

    private sealed interface ResolverWidgetKey {

        record OfGenericResolver(WidgetResolver widgetResolver) implements ResolverWidgetKey {
        }

        // TODO ennek jobb key kéne
        record OfPeerSpecificResolver(PeerRequestor.Request<?> request) implements ResolverWidgetKey {
        }

        record OfFallback() implements ResolverWidgetKey {
        }
    }

}
