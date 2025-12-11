package ui11;

import ui11.observable.ObserverHolder;
import ui11.provide.UpValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

// TODO zavaros, hogy implements Widget.
// két dologra van/volt használva widgetként:
// - ha build()-ből returnöljük (pl. J2DPointerRegionPeer): egy (vagy több, de statikusan felsorolható) capability-t
//   (pl. rendering node) selfből exposeolunk, míg minden más capability-t az illető childből.
//   Ennek a kiváltására kéne csinálni egy capability routing API-t, ami alaposabb annál ami most van,
//   hogy csak végigmegyünk a delegate láncon.
// - ha megadjuk egy másik widgetnek (pl. ui-layout-constraint, bár onnan épp ki lett szedve): nem akarjuk
//   újra instantiálni a widgetet, de valszeg csak sebesség miatt. ilyenkor ha WidgetInstantiationt adunk át,
//   az azért problémás, mert elvesznek az IV-k (akár azok a Provider objektumokban megadottak, akár a
//   leszármazott Element által örököltek).

/**
 * A reference to instantiation of a {@linkplain Widget}.
 */
public final class WidgetInstantiation {

    private final Element container;
    private final BuildContext refresh;

    /**
     * akkor null, ha a lánc végén nem Element van, hanem UpValue null next-tel (pl. J2DColorPrimitive)
     */
    @Nullable final Element element;

    final List<? extends UpValue> upValues;

    // delegate visszarakáshoz kellő dolgok.
    // ha nem delegate-et képzünk vagy nem Element lett belőle, akkor mindegy, hogy mit rakunk beléjük.
    final KeyWrapper key;
    final Map<Class<?>, Object> ivs;

    WidgetInstantiation(Element container, BuildContext refresh, @Nullable Element element,
                        List<? extends UpValue> upValues, KeyWrapper kw, Map<Class<?>, Object> ivs) {
        this.container = container;
        this.refresh = refresh;
        this.element = element;
        this.upValues = upValues;
        this.key = kw;
        this.ivs = ivs;
    }

    // és meddig maradjon aktív a megadott elem?

    void ensureFresh() {
        checkIsValid();
        switch (element.elementState) {
            // itt jó volt hogy volt REFRESH_REQUESTED_FOR_CHILDREN is.
            // lehet hogy majd vissza kéne rakni
            case IDLE -> { // IDLE_STOPPABLE nem lehet, mert checkIsValid nem teljesülne
            }
            case REFRESHING_SELF_BEFORE_CHILDREN,
                 REFRESHING_SELF_AFTER_CHILDREN,
                 REFRESHING_CHILDREN_AFTER_SELF,
                 REFRESHING_CHILDREN_AFTER_NO_SELF,
                 REFRESHING_CHILDREN_AFTER_NO_SELF_BUT_SELF_REQUESTED_IN_DESCENDANTS,
                 REFRESHING_CHILDREN_SECOND -> throw new IllegalStateException();
            case START_REQUESTED, RESTART_REQUESTED, REFRESH_REQUESTED -> {
                ObserverHolder.withoutObserver(element::refresh);
            }
            default -> {
                // nem lehetséges
                throw new RuntimeException("should not reach here " +
                        "(" + element + ", " + element.elementState + ")");
            }
        }
    }

    /**
     * A delegate láncon végighaladva keres egy olyan Widgetet vagy Elementet, mely implementálja a megadott osztályt
     * vagy interface-t, és visszaadja azt. Ha több ilyen is van, akkor a legelsőt.
     *
     * @throws IllegalStateException  ha már vége lett annak a {@link Element#build() buildnek}, mely során
     *                                ez az WidgetInstantiation keletkezett, vagy ha időközben az ezt a widgetet
     *                                példányosító Element egy leszármazottja is példányosított és ugyanaz az Element
     *                                keletkezett
     * @throws NoSuchElementException ha nem találtunk a keresési feltételnek megfelelő Widgetet vagy Elementet a
     *                                delegate láncban
     */
    @Nonnull
    public <U extends UpValue> U lookup(Class<U> type) {
        Objects.requireNonNull(type);
        if (type == UpValue.class || !UpValue.class.isAssignableFrom(type))
            throw new IllegalArgumentException("not an " + UpValue.class.getSimpleName() + " subtype: " + type.getName());

        return doLookup(type, false);
    }

    /**
     * A delegate láncon végighaladva keres egy olyan Widgetet vagy Elementet, mely implementálja a megadott osztályt
     * vagy interface-t, és visszaadja azt. Ha több ilyen is van, akkor a legelsőt.
     *
     * @return {@linkplain Optional#empty()}, ha nem találtunk a keresési feltételnek megfelelő Widgetet vagy Elementet
     * a delegate láncban
     * @throws IllegalStateException ha már vége lett annak a {@link Element#build() buildnek}, mely során
     *                               ez az WidgetInstantiation keletkezett, vagy ha időközben az ezt a widgetet
     *                               példányosító Element egy leszármazottja is példányosított és ugyanaz az Element
     *                               keletkezett
     */
    public <U extends UpValue> Optional<U> lookupOptional(Class<U> type) {
        return Optional.ofNullable(doLookup(type, true));
    }

    private <U extends UpValue> U doLookup(Class<U> type, boolean optional) {
        checkIsValid();
        if (element != null)
            ensureFresh();

        container.upValuesIP.subscribe();
        if (element == null) {
            U t = Element.findInUpValueList(type, upValues);
            if (t == null && !optional)
                throw new NoSuchElementException(type.getName() + " not found in delegate chain of " + this +
                        "\nDirect up values: " + upValues);
            else
                return t;
        } else {
            U value = element.lookupImpl(type, false, optional);
            element.parentInterestedUpValues.put(type, value);
            return value;
        }
    }

    private void checkIsValid() {
        if (refresh != container.refreshState) // ellenőrzi, hogy REFRESH_SELF-e
            throw new IllegalStateException();
        if (element != null && element.parent != container)
            throw new IllegalStateException();
    }

 /*
    @SafeVarargs
    public <U> U lookup(Class<? extends U>... c) {
    }
 */
}
