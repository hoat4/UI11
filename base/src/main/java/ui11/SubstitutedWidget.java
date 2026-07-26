package ui11;

import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;

import static java.util.stream.Collectors.joining;

// időnként felmerül, hogy ezt jó lenne külön package-be vinni, WidgetResolver és GlobalWidgetResolvers mellé.
// Először azért volt problémás, mert használta findInheritedValueForInjection-t egyrészt a WidgetResolver
// lookupolásához, másrészt a ResolutionContext::inherited implementálásához. ez megoldódott, át is került
// ui11.resolution package-be.
// De aztán jött hogy legyen mint SubstitutedWidget upvalue is egyben, meg az egész PeerCreationRequest dolog,
// és így meg rengeteg hivatkozás lenne package-ek között, ezért inkább visszraktam a root package-be.

/**
 * A stateless widget which does not determine its content by itself, instead a {@link WidgetResolver} is asked for
 * which content to display in it.
 * <p>
 * A {@linkplain SubstitutedWidget} must not contain fields annotated with {@link ui11.Widget.Inject @Inject} or
 * {@link Widget.Remember @Remember}.
 * <p>
 * If a {@linkplain SubstitutedWidget} is handled by a {@link PeerCreationRequest}, then it doesn't build more widgets.
 */
public abstract class SubstitutedWidget extends Widget {

    @Inject(required = false) private WidgetResolver widgetResolver;
    @Inject private ResolutionRequestCollection peerCreationRequestCollection;

    // TODO ezt csak akkor kéne lekérdezni és observálni, ha resolutionRequest.requestData.peerType().isInstance(this)
    @Inject(required = false) private ParentDataWidget.ParentDataCollection parentDataCollection;

    /**
     * It is final because there are no {@link Remember state fields} permitted in the subclasses, so it not sensible
     * for the subclasses to do anything in this method.
     */
    @Override
    protected final void initState() {
    }

    @Override
    protected final void onResume() {
    }

    @Override
    protected final Widget build() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("ConstantValue")
    Widget build2(Set<ResolutionRequest<?>> completedReqsDst) {
        SubstitutedWidget potentialPeer =
                this instanceof ParentDataWidget.CombinerParentDataWidget c ? c.parentData : this;
        boolean allCompleted = true;
        for (ResolutionRequest<?> resolutionRequest : peerCreationRequestCollection.remainingRequests()) {
            if (resolutionRequest.requestData.peerType().isInstance(potentialPeer)) {
                List<? extends ParentDataWidget> parentDataList =
                        parentDataCollection == null ? List.of() : parentDataCollection.parentDataList;
                // TODO ha már kapott resultot ebben a refreshben, akkor az újabbakat ignorálnia kéne vagy beraknia?
                resolutionRequest.setResultUnchecked(potentialPeer, parentDataList,
                        widgetState().tree.beganRefreshID);
                completedReqsDst.add(resolutionRequest);
            } else
                allCompleted = false;
        }
        if (allCompleted)
            return null;

        Widget resolved = null;

        if (widgetResolver != null)
            resolved = widgetResolver.resolveOrNull(this);

        if (resolved == null)
            resolved = GlobalWidgetResolvers.instance().resolveOrNull(this, peerCreationRequestCollection);

        if (resolved == null) {
            resolved = fallbackContent();
            if (resolved == null)
                // nem sikerült peert létrehozni. abban bízunk, hogy resolveAdditional-ök által berakott
                // ParentDataWidgetek egyike jó lesz, ezért "halogatjuk" az exception dobását.
                // ha tényleg jó lesz az egyik, akkor ott megszakad a lánc refreshe.
                resolved = new NoPeerFactoryAvailable(getClass(), peerCreationRequestCollection);
        }


        WidgetResolver wr = GlobalWidgetResolvers.instance();
        if (widgetResolver != null)
            wr = WidgetResolver.composite(wr, widgetResolver);

        for (ResolutionRequest<?> req : peerCreationRequestCollection.remainingRequests()) {
            resolved = wr.resolveAdditional(this, req.requestData, resolved);
            if (resolved == null)
                throw new NullPointerException(
                        WidgetResolver.class.getSimpleName() + ".resolveAdditional returned null\n" +
                                "Widget: " + this + "\n" +
                                "Resolver: " + wr /* TODO */);
        }

        return resolved;
    }

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
                    "Remaining requests: " + requests.remainingRequests() + "\n" +
                    "Completed requests: " + requests.completedRequests() + "\n" +
                    "Refresh stack: " + widgetState().tree.refreshStackToDebugString());
        }
    }
}
