package ui11;

import org.jspecify.annotations.NonNull;
import ui11.observable.MutableObservable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO név? IdentityPreservingSlot jutott eszembe először, de az nem érthető
//      esetleg StateHolder?
public final class Slot2 {

    private final MutableObservable<Widget> content = MutableObservable.ofNullable();
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
        return content == null ? null : w;
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

    public static final class Slot2List {

        private final List<Slot2> slots = new ArrayList<>();

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

    public static final class Slot2Map<K> {

        private final Map<K, Slot2> slots = new HashMap<>();

        public @NonNull Map<@NonNull K, ? extends @NonNull Widget> with(
                @NonNull Map<@NonNull K, ? extends @NonNull Widget> widgets) {
            widgets = Map.copyOf(widgets);

            Map<K, Widget> result = new HashMap<>();

            widgets.forEach((key, widget) -> {
                result.put(key, slots.computeIfAbsent(key, __ -> new Slot2()).with(widget));
            });
            slots.keySet().retainAll(widgets.keySet());

            return Map.copyOf(result);
        }
    }
}
