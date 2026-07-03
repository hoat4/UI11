package ui11;

import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

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
    @Inject private PeerCreationRequestCollection peerCreationRequestCollection;

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

    @SuppressWarnings("ConstantValue")
    @Override
    protected final Widget build() {
        PeerCreationRequest<?> peerCreationRequest = peerCreationRequestCollection.request;
        if (peerCreationRequest.peerType().isInstance(this))
            return null; // Elementben special case-elve van SubstitutedWidget, hogy build adhat vissza nullt

        Widget resolved = null;

        if (widgetResolver != null)
            resolved = widgetResolver.resolveOrNull(this);

        if (resolved == null)
            resolved = GlobalWidgetResolvers.instance().resolveOrNull(this, peerCreationRequestCollection);

        if (resolved == null) {
            resolved = fallbackContent();
            if (resolved == null)
                throw new RuntimeException("no " + WidgetResolver.class.getSimpleName() + " supports " +
                        getClass().getName() + " and " + ReflectionUtil.simpleName(getClass()) +
                        ".fallbackContent() returned null");
        }

        resolved = GlobalWidgetResolvers.instance().resolveAdditional(this, resolved, peerCreationRequestCollection);

        if (widgetResolver != null) {
            resolved = widgetResolver.resolveAdditional(this, resolved);
            if (resolved == null)
                throw new NullPointerException(
                        WidgetResolver.class.getSimpleName() + ".resolveAdditional returned null\n" +
                                "Widget: " + this + "\n" +
                                "Resolver: " + widgetResolver);
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
}
