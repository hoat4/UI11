package ui11;

// LCW-t meg lehetne próbálni, hogy ne legyenek olyan távol a Slot definíciók a használatuktól.
// StackWalker használatán is gondolkodtam, de nem találtam értelmes felhasználást neki.

// TODO le kéne írni, hogy a tipikus layout konténereknél nem kell ezt használni

/**
 * A Slot holds the state of an underlying Widget.
 * <p>
 * A new widget will only be used to update an existing element if its slot is the same as the slot of the current
 * widget
 * associated with the element.
 * <p>
 * A Slot can be obtained using the {@link ui11.Widget.Inject @Inject} annotation in a Widget or by
 * {@linkplain MultiSlot#get(Object)}.
 *
 * @see MultiSlot
 */
public final class Slot {

    // azért nem Element, hogy lehessen ellenőrizni, hogy még aktív-e
    final WidgetState<?> slotContainerWidget;

    // TODO lazy
    final Element element = new Element();

    /**
     *
     * @param w csak akkor null, ha {@link Element#delegateSlot}-ban használjuk
     */
    Slot(WidgetState<?> w) {
        this.slotContainerWidget = w;
    }
}
