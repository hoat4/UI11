package ui11;

import ui11.observable.ObserverHolder;

import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * A reference to instantiation of a {@linkplain Widget}.
 */
final class WidgetInstantiation {

    private final Element container;

    /**
     * akkor null, ha a lánc végén nem Element van, hanem ending widget (pl. J2DColorPrimitive)
     */
    @Nullable final Element element;

    WidgetInstantiation(Element container, @Nullable Element element) {
        this.container = container;
        this.element = element;
    }

    // és meddig maradjon aktív a megadott elem?

    void ensureFresh() {
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

 /*
    @SafeVarargs
    public <U> U lookup(Class<? extends U>... c) {
    }
 */
}
