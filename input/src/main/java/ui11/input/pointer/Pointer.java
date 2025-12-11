package ui11.input.pointer;

import ui11.geom.Location;

import java.util.Set;

/**
 * Egérmutató vagy folyamatban lévő érintés, amely képernyőn lévő pontra mutat, és amelytől le lehet kérdezni hogy
 * melyik pontra mutat és hogy milyen gombok vannak rajta benyomva. Lehet {@link MouseCursor egérkurzor}, illetve
 * {@link Touch érintés}. Ha a számítógéphez több egér van csatlakoztatva, akkor is csak egy egérkurzor példány van,
 * viszont multi-touch kijelző esetén a párhuzamos ujjakat külön {@linkplain Touch} típusú Pointer objektumok
 * reprezentálják.
 */
public interface Pointer {

    /**
     * @return unmodifiable set
     */
    // TODO lehet hogy inkább OSetet kéne visszaadni, nem unmodifiablét
    Set<? extends Button> pressedButtons();

    // TODO ez mit csináljon ha még nem elérhető a hely?
    Location location();

    interface Button {
    }

    enum StandardMouseButton implements Button {

        /**
         * Represents primary (button 1, usually the left) mouse button.
         */
        PRIMARY,

        /**
         * Represents middle (button 2) mouse button.
         */
        MIDDLE,

        /**
         * Represents secondary (button 3, usually the right) mouse button.
         */
        SECONDARY,

        /**
         * Represents back (button 4) mouse button.
         */
        BACK,

        /**
         * Represents forward (button 5) mouse button.
         */
        FORWARD
    }

    interface MouseCursor extends Pointer {
    }

    interface Touch extends Pointer {
    }

    // public enum ExtendedTouchPenButton implements PointerButton { ERASE }
}
