package ui11;

import org.jspecify.annotations.NonNull;
import ui11.observable.MutableObservable;

import java.util.*;

// TODO név? IdentityPreservingSlot jutott eszembe először, de az nem érthető
//      esetleg StateHolder?

// kommentek régi Slotból:

// LCW-t meg lehetne próbálni, hogy ne legyenek olyan távol a Slot definíciók a használatuktól.
// StackWalker használatán is gondolkodtam, de nem találtam értelmes felhasználást neki.

// TODO le kéne írni, hogy a tipikus layout konténereknél nem kell ezt használni

/**
 * A Slot is a widget which have identity: if it is inserted into an other position in the tree,
 * its state will be retained.
 */
// WidgetTree.findOrCreateWidgetState-ben special case-elve van ez a widget, hogyha SlotWidgetet
// talál, akkor ignorálja a previous WidgetInstantiationt és a KeyWrappereket is
public final class Slot2 extends Widget {

    // IntelliJ IDEA-hoz hack, hogy with-et ne tekintse nullable-nek
    private static final Slot2 SLOT_WIDGET_NULL = null;

    final MutableObservable<Widget> content = MutableObservable.ofNullable();

    /**
     * Creates a new Slot which will be initially empty.
     */
    public Slot2() {
    }

    /**
     * Changes the content of this slot widget.
     * @return {@code null} if the parameter is {@code null}, {@code this} otherwise
     */
    // itt azért nincs return typeon nullability megadva, mert
    // akkor nemnull, ha param type is nemnull
    public Widget with(Widget content) {
        // TODO ha egyszerre több helyen próbálják használni ugyanazt a slotot de eltérő contenttel, azt
        //      kéne tudnunk detektálni és jelezni valahogy?

        this.content.set(content);
        return content == null ? SLOT_WIDGET_NULL : this;
    }

    @Override
    protected Widget build() {
        Widget w = content.get();
        if (w == null)
            // ha soha nem is lett volna, akkor nem lyukadunk ki ide
            throw new RuntimeException("Content has been removed");
        return w;
    }

    String debugInfo() {
        if (content.snoop() == null)
            return super.toString() + " <empty>";
        if (content.snoop().getClass().getSimpleName().equals("J2DPathShapedPeer")) // TODO
            return super.toString() + " " + content.snoop();
        return super.toString() + " " + (content.snoop() == null ? "<empty>" :
                "containing " + content.snoop().getClass().getName());
    }

    /**
     * A collections of {@linkplain Slot2 Slots} that are indexed by an int.
     * <p>
     * This is usually used when a widget needs to show a list of items: each row gets a different slot, so when some row
     * changes, only the changed row refreshed, not the container of all items.
     */
    public static final class SlotList {

        private final List<Slot2> slots = new ArrayList<>();

        // TODO itt is lehetne detektálni a dupla meghívásokat (lehet akár véletlen is,
        //      pl. TabbedPaneben van 2 db SlotList)
        public @NonNull List<? extends Widget> with(@NonNull List<? extends @NonNull Widget> widgets) {
            List<Widget> result = new ArrayList<>();

            int i = 0;
            for (Widget w : widgets) {
                Slot2 s;
                if (slots.size() == i)
                    slots.add(s = new Slot2());
                else
                    s = slots.get(i);
                i++;
                result.add(s.with(w));
            }

            slots.subList(i, slots.size()).clear();

            return List.copyOf(result);
        }
    }

    // TODO ennek az API-ján még vacakolni kell, mert gyakorlati célokra (pl. adattáblán belüli key-ek)
    //      most elég nehézkesen használható, csak SubstitutedWidgetekre használható könnyen

    /**
     * A collection of {@linkplain Slot2 Slots} that are indexed by an arbitrary typed key.
     * <p>
     * This is usually used when a widget needs to show a list of items: each item gets a different slot, so when some
     * row changes, only the changed row refreshed, not the container of all items.
     */
    public static final class SlotMap<K> {

        private final Map<K, Slot2> slots = new HashMap<>();

        public @NonNull Map<@NonNull K, ? extends @NonNull Widget> with(
                @NonNull Map<@NonNull K, ? extends @NonNull Widget> widgets) {
            HashMap<K, Widget> m = new HashMap<>(widgets);

            m.replaceAll((key, widget) ->
                    slots.computeIfAbsent(key, __ -> new Slot2()).with(widget));
            slots.keySet().retainAll(widgets.keySet());

            return Collections.unmodifiableMap(m);
        }

        public @NonNull SequencedMap<K, ? extends Widget> with(
                @NonNull SequencedMap<K, ? extends Widget> widgets) {
            LinkedHashMap<K, Widget> m = new LinkedHashMap<>(widgets);

            m.replaceAll((key, widget) ->
                    slots.computeIfAbsent(key, __ -> new Slot2()).with(widget));
            slots.keySet().retainAll(widgets.keySet());

            return Collections.unmodifiableSequencedMap(m);
        }
    }

    /**
     * A collections of {@link Slot2 Slots} that are cached by a key.
     * <p>
     * This is usually used when a widget needs to show a list of items: each row gets a different slot, so when some row
     * changes, only the changed row refreshed, not all items.
     *
     * @param <K> the type of the cache keys
     *
    public static final class SlotCache<K> {
    ...
    }
     */

    // 2025-11-08-ból megjegyzés keyekről (akkor még Elementben voltak):
    //      withKey dobjon exceptiont ha duplicate
    //      De ez nem olyan egyszerű. Mit tekintünk duplicate-nek?
    //      - ha többször hívják meg ugyanazzal a kulccsal. De mi van, ha refreshSelf-en kívül van meghívva?
    //      - többször van felhasználva a KeyWrapper. Ez se jó, mert ez lehet legális is:
    //        pl. MultiChildLayoutImpl első alkalommal instantiateeli mérésre, második alkalommal meg berakja
    //        Overlay/Transform-ba childnak.
}
