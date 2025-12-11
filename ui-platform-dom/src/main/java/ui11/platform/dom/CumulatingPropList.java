package ui11.platform.dom;

import ui11.control.Tooltip;
import ui11.input.focus.FocusListener;
import ui11.input.pointer.WithCursor.Cursor;
import ui11.input.pointer.PointerRegion;
import ui11.provide.Provider.Mergeable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Provider/@Inherited-del használhandó típus. CSSClassTag meg egyebek van erre decomposeolva DOM backend esetén.

// "Tag" interface felett volt egy ilyen komment (2025-12-05-ben törölve lett az az interface):
// kéne valami Element.findTags (felmenőkben keres adott típusú Tagokat).
//      most ennek a hiánya miatt kell pl. DOM platformon a CumulatingPropList kavarás.
//      csak akkor kéne valami "consume" mechanizmus is hozzá, ami alapján eldönthető, hogy meddig keressünk.
//      r23190-ben olyasmire gondoltam, hogy @Inheritable(until=...), de lehet hogy inkább addig kéne felmenni,
//      amíg nem érünk el egy olyan widgetet, ahol nincs már findTags hívva.

public record CumulatingPropList(Set<String> cssClasses,
                                 List<Runnable> onClick,
                                 List<FocusListener> onFocus,
                                 List<PointerRegion> pointerRegions,
                                 List<Cursor> cursors,
                                 List<Tooltip> tooltipTags,
                                 boolean hidden
) implements Mergeable<CumulatingPropList> {

    public CumulatingPropList {
        cssClasses = Set.copyOf(cssClasses);
        onClick = List.copyOf(onClick);
        onFocus = List.copyOf(onFocus);
        pointerRegions = List.copyOf(pointerRegions);
        cursors = List.copyOf(cursors);
        tooltipTags = List.copyOf(tooltipTags);
    }

    public static CumulatingPropList ofCSSClass(String className) {
        return new CumulatingPropList(Set.of(className),
                List.of(),List.of(),List.of(),List.of(),List.of(), false);
    }

    public static CumulatingPropList ofOnClick(Runnable onClick) {
        return new CumulatingPropList(Set.of(),
                List.of(onClick), List.of(),List.of(),List.of(),List.of(), false);
    }

    public static CumulatingPropList ofFocus(FocusListener onFocus) {
        return new CumulatingPropList(Set.of(), List.of(),
                List.of(onFocus),List.of(),List.of(),List.of(), false);
    }

    public static CumulatingPropList ofPointerRegion(PointerRegion pointerRegion) {
        return new CumulatingPropList(Set.of(), List.of(), List.of(),
                List.of(pointerRegion),List.of(),List.of(), false);
    }

    public static CumulatingPropList ofCursor(Cursor cursor) {
        return new CumulatingPropList(Set.of(), List.of(), List.of(), List.of(),
                List.of(cursor),List.of(), false);
    }

    public static CumulatingPropList ofTooltipTag(Tooltip tooltip) {
        return new CumulatingPropList(Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(tooltip), false);
    }

    public static CumulatingPropList ofHidden() {
        return new CumulatingPropList(Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), true);
    }

    @Override
    public CumulatingPropList mergeWith(CumulatingPropList defaults) {
        if (this == CLEAR || defaults == CLEAR)
            return this;
        return new CumulatingPropList(
                concat(defaults.cssClasses, this.cssClasses),
                concat(defaults.onClick, this.onClick),
                concat(defaults.onFocus, this.onFocus),
                concat(defaults.pointerRegions, this.pointerRegions),
                concat(defaults.cursors, this.cursors),
                concat(defaults.tooltipTags, this.tooltipTags),
                defaults.hidden || this.hidden
        );
    }

    private static <T> Set<T> concat(Set<T> a, Set<T> b) {
        if (a.isEmpty())
            return b;
        if (b.isEmpty())
            return a;
        Set<T> set = new HashSet<>(a);
        set.addAll(b);
        return Set.copyOf(set);
    }

    private static <T> List<T> concat(List<T> a, List<T> b) {
        if (a.isEmpty())
            return b;
        if (b.isEmpty())
            return a;
        int aSize = a.size(), bSize = b.size();
        Object[] elements = a.toArray(new Object[aSize + bSize]);
        for (int i = 0; i < bSize; i++) {
            elements[aSize + i] = b.get(i);
        }
        @SuppressWarnings("unchecked") List<T> resultList = (List<T>) List.of(elements);
        return resultList;
    }

    static final CumulatingPropList CLEAR = new CumulatingPropList(
            Set.of(), List.of(), List.of(), List.of(), List.of(), List.of(), false);
}
