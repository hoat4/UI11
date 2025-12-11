package ui11;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Element.IVNotProvided;
import ui11.Element.InheritedValueHolder.IVUsage;
import ui11.provide.DynamicProvider;
import ui11.provide.Provider;
import ui11.provide.Provider.Mergeable;
import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;

import javax.annotation.Nullable;
import java.util.*;

// peer cacheelés:
// - ha van key, az utolsó ("legbelső") keyhez tartozó peert elkérjük, ha
//   használható (trySetModel true-t adott vissza), azt használjuk, különben eldobjuk
// - ha nincs key, widget type alapján keresünk egyet a containerben
// korábban az volt, hogy minden key számított, de ez nehezen követkető volt, és vé-
// gül azért lett kiszedve, mert explicit keyeket akartunk használni pl. UploadPictureMain-ben
// arra hogy kényszerítsük új Node létrehozását.

class ChainSegmentBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ChainSegmentBuilder.class);

    private final Element container;
    private final Widget initialWidget;

    private final List<UpValue> upValues = new ArrayList<>();
    private final Map<Class<?>, Object> ivs = new HashMap<>();
    private KeyWrapper key;

    public ChainSegmentBuilder(Element container, Widget initialWidget, KeyWrapper implicitKey) {
        this.container = container;
        this.initialWidget = initialWidget;
        this.key = implicitKey;
    }

    public WidgetInstantiation build() {
        Widget currentWidget = initialWidget;

        while (true) {
            Objects.requireNonNull(key);
            switch (currentWidget) {
                case UpValueWrapper upValueWrapper -> {
                    upValues.add(upValueWrapper.value());
                    if (upValueWrapper.next() == null) {
                        return new WidgetInstantiation(container, container.refreshState, null, upValues,
                                key, ivs);
                    }

                    // azért nem upValueWrapper.next, hogy képződjön egy újabb Element, ami tartalmazza ezt az upvaluet
                    // TODO ha a láncszegmens vége nem Element lesz, akkor felesleges volt ez
                    return handleRegularWidget(upValueWrapper);
                }
                case null -> {
                    throw new NullPointerException("CSB null " + container + ", " + upValues);
                }
                case Provider<?> p -> currentWidget = handleProvide(p);
                case KeyWrapper kw -> {
                    key = kw;
                    currentWidget = kw.content;
                }
                default -> {
                    return handleRegularWidget(currentWidget);
                }
            }
        }
    }

    private Widget handleProvide(Provider<?> p) {
        Object val = p.value();

        // részben azért nem val instanceof Mergeable-t nézünk, hogy null esetén is működjön,
        // részben pedig hogy findIVProvidesUntil nem a példány típusából, hanem a megadott típusból
        // dönti el, hogy directIVsből vagy a directAncestorEDs-ből szedje az értékeket.
        if (Mergeable.class.isAssignableFrom(p.type()) || p.type() == DynamicProvider.class) {
            // DynamicProvider "kvázi-mergeable"

            if (p.value() == null) {
                // p.type() == ViewProvider.class-rel nem kell foglalnozni,
                // mert VP <: Mergeable nem teljesül
                return p.content();
            }

            Object prevVal;
            if (ivs.containsKey(p.type()))
                prevVal = ivs.get(p.type());
            else
                prevVal = container.findInheritedValue(p.type(), IVUsage.USED_BY_SELF);
            // TODO nem kéne a containert folyton refreshelni ha megváltozik az mergekor használt örökölt érték egy
            //      része
            if (prevVal != IVNotProvided.IV_NOT_PROVIDED) {
                if (p.type() == DynamicProvider.class)
                    val = mergeDynamicProviders((DynamicProvider) prevVal, (DynamicProvider) p.value());
                else {
                    @SuppressWarnings("unchecked")
                    Object merged = ((Mergeable<Object>) val).mergeWith(prevVal);
                    if (merged == null || merged.getClass() != val.getClass())
                        // azért nem engedjük, mert findIVProvidesUntil belezavarodna, hogy
                        // az ivsből vagy a mergeableIVsből szedje.
                        // ha ezt mégis engedjük, akkor is annyit legalább kéne ellenőrizni,
                        // hogy p.type().isInstance(val) (vagy null)
                        throw new RuntimeException("Mergeable returned with different type: " + val + ", " +
                                prevVal + " -> " + merged +
                                " (" +
                                val.getClass().getName() + ", " +
                                (prevVal == null ? "null" : prevVal.getClass().getName()) + " -> " +
                                (merged == null ? "null" : merged.getClass().getName())
                                + ")");

                    val = merged;
                }
            }
        }

        ivs.put(p.type(), val);
        return p.content();
    }

    private DynamicProvider mergeDynamicProviders(DynamicProvider prev, DynamicProvider value) {
        Objects.requireNonNull(prev);
        Objects.requireNonNull(value);

        // TODO ha valamelyik már mergeölt, akkor nem kéne újat csinálni, mert SOE lehet ha túl sok van belőlük
        return new DynamicProvider() {
            @Nullable
            @Override
            public <T> T provideOrNull(Class<T> type) {
                T t = value.provideOrNull(type);
                return t != null ? t : prev.provideOrNull(type);
            }

            @Override
            public String toString() {
                return "Merged " + DynamicProvider.class.getSimpleName() + "s {prev=" + prev + ", new=" + value + "}";
            }
        };
    }

    private WidgetInstantiation handleRegularWidget(Widget w) {
        Element peer = key.container.cachedPeers.get(key.key);

        if (peer == null || !peer.trySetModel(w)) {
            peer = new RSWStateHolder<>(w.accessor());
            peer.setModel(w);
        }

        return container.registerChild(peer, key, upValues, ivs);
    }
}
