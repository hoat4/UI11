package ui11.graphics;

import ui11.resolution.SubstitutedWidget;

// ez azért ebben a packageben van az ui11.graphics.fill helyett, mert a fill-ek
// átlátszatlanok az egér szempontjából, ez viszont átlátszó

// TODO Gone-ra, LinearLayout#gap-re és PointerOpaque-ra nem lehet hivatkozni, mert más modulban vannak

/**
 * Empty widget. It takes up minimal space what enabled by the parent.
 * <p>
 * If the parent is a layout container where gaps are set, there will be gaps on both sides. To collapse the gaps, use
 * {@code Gone} instead.
 * <p>
 * This is transparent for pointers (mouse, touch). To not let through pointer interactios, use a
 * {@code ui11.input.pointer.PointerOpaque} widget.
 */
public final class Empty extends SubstitutedWidget {

    private static final Empty INSTANCE = new Empty();

    private Empty() {
        if (INSTANCE != null)
            throw new Error();
    }

    public static Empty empty() {
        return INSTANCE;
    }

    @Override
    public String toString() {
        return "Empty";
    }
}
