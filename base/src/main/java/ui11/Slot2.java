package ui11;

import org.jspecify.annotations.NonNull;
import ui11.observable.MutableObservable;

import java.util.*;

// TODO név? IdentityPreservingSlot jutott eszembe először, de az nem érthető
//      esetleg StateHolder?
public final class Slot2 {

    // IntelliJ IDEA-hoz hack, hogy with-et ne tekintse nullable-nek
    private static final SlotWidget SLOT_WIDGET_NULL = null;

    final MutableObservable<Widget> content = MutableObservable.ofNullable();
    private final SlotWidget w;

    WidgetState<SlotWidget> widgetState;

    public Slot2() {
        this.w = new SlotWidget(this);
    }

    // itt azért nincs return typeon nullability megadva, mert
    // akkor nemnull, ha param type is nemnull
    public Widget with(Widget content) {
        // TODO ha egyszerre több helyen próbálják használni ugyanazt a slotot de eltérő contenttel, azt
        //      kéne tudnunk detektálni és jelezni valahogy?

        this.content.set(content);
        return content == null ? SLOT_WIDGET_NULL : w;
    }

    String debugInfo() {
        if (content.snoop() == null)
            return super.toString() + " <empty>";
        if (content.snoop().getClass().getSimpleName().equals("J2DPathShapedPeer")) // TODO
            return super.toString() + " " + content.snoop();
        return super.toString() + " " + (content.snoop() == null ? "<empty>" :
                "containing " + content.snoop().getClass().getName());
    }

    // WidgetTree.findOrCreateWidgetState-ben special case-elve van ez a widget, hogyha SlotWidgetet
    // talál, akkor ignorálja a previous WidgetInstantiationt és a KeyWrappereket is
    static class SlotWidget extends Widget {

        final Slot2 slot;

        private SlotWidget(Slot2 slot) {
            this.slot = slot;
        }

        @Override
        protected Widget build() {
            Widget w = slot.content.get();
            if (w == null)
                // ha soha nem is lett volna, akkor nem lyukadunk ki ide
                throw new RuntimeException("Content has been removed");
            return w;
        }
    }

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
}
