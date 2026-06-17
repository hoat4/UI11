package ui11;

import ui11.observable.ObserverHolder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A reference to instantiation of a {@linkplain Widget}.
 */
final class WidgetInstantiation {

    private final Element container;
    final Element.RefreshID refresh;

    /**
     * akkor null, ha a lánc végén nem Element van, hanem ending widget (pl. J2DColorPrimitive)
     */
    @Nullable final Element element;

    final List<? extends SubstitutedWidget> upValues;

    WidgetInstantiation(Element container, Element.RefreshID refresh, @Nullable Element element,
                        List<? extends SubstitutedWidget> upValues) {
        Objects.requireNonNull(upValues);
        this.container = container;
        this.refresh = refresh;
        this.element = element;
        this.upValues = upValues;
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
     * @throws IllegalStateException  ha már vége lett annak a {@linkplain ui11.Element.RefreshID refreshSelfnek}, mely
     *                                során ez az WidgetInstantiation keletkezett, vagy ha időközben az ezt a widgetet
     *                                példányosító Element egy leszármazottja is példányosított és ugyanaz az Element
     *                                keletkezett
     * @throws NoSuchElementException ha nem találtunk a keresési feltételnek megfelelő Widgetet vagy Elementet a
     *                                delegate láncban
     */
    public <U extends SubstitutedWidget> @NonNull U lookup(Class<U> type) {
        Objects.requireNonNull(type);
        if (type == SubstitutedWidget.class || !SubstitutedWidget.class.isAssignableFrom(type))
            throw new IllegalArgumentException("not an " + SubstitutedWidget.class.getSimpleName() + " subtype: " + type.getName());

        return doLookup(type, false);
    }

    /**
     * A delegate láncon végighaladva keres egy olyan Widgetet vagy Elementet, mely implementálja a megadott osztályt
     * vagy interface-t, és visszaadja azt. Ha több ilyen is van, akkor a legelsőt.
     *
     * @return {@linkplain Optional#empty()}, ha nem találtunk a keresési feltételnek megfelelő Widgetet vagy Elementet
     * a delegate láncban
     * @throws IllegalStateException ha már vége lett annak a {@linkplain ui11.Element.RefreshID refreshSelfnek}, mely
     *                               során ez az WidgetInstantiation keletkezett, vagy ha időközben az ezt a widgetet
     *                               példányosító Element egy leszármazottja is példányosított és ugyanaz az Element
     *                               keletkezett
     */
    public <U extends SubstitutedWidget> Optional<U> lookupOptional(Class<U> type) {
        return Optional.ofNullable(doLookup(type, true));
    }

    private <U extends SubstitutedWidget> U doLookup(Class<U> type, boolean optional) {
        checkIsValid();
        if (element != null) {
            // mivel épp most frissítjük a parentet, ezért nem kell külön értesíteni próbálni őt a változásokról,
            // mert értesülni fog róluk az e függvény által visszaadott értékből
            element.parentInterestedUpValues.clear();

            ensureFresh();
        }

        container.upValuesIP.subscribe();
        U t = Element.findInUpValueList(type, upValues);
        if (t != null)
            // ha nem a child provideolta ezt az upValuet, akkor ne rakjuk be parentInterestedUpValuesba
            return t;
        if (element == null) {
            if (!optional)
                throw new NoSuchElementException(type.getName() + " not found in delegate chain of " + this +
                        "\nDirect up values: " + upValues);
            else
                return null;
        } else {
            U value = element.lookupImpl(type, false, optional);
            element.parentInterestedUpValues.put(type, value);
            return value;
        }
    }

    private void checkIsValid() {
        if (refresh != container.refreshID) // ellenőrzi, hogy REFRESH_SELF-e
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
