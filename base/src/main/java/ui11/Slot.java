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

    final WidgetTree tree;

    WidgetState<?> content;

    Slot(WidgetTree tree) {
        this.tree = tree;
    }
}
