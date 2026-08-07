package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.observable.MutableObservable;

import java.util.*;

/**
 * A Key is an identifier for Widgets.
 * <p>
 * A new widget will only be used to update an existing widget state if its key is the same as the key of the
 * current widget; otherwise, a new widget state will be created.
 */
public sealed abstract class Key {

    Key() {
    }

    /**
     * Creates a <a href="https://api.flutter.dev/flutter/widgets/GlobalKey-class.html">global key</a>.
     * <p>
     * Global keys should not be re-created on every build.
     * They should usually be long-lived objects stored in a {@link ui11.Widget.Remember @Remember} field, for example.
     * Creating a new GlobalKey on every build will throw away the state of the subtree associated with the old key and
     * create a new fresh subtree for the new key.
     */
    public static @NonNull Key create() {
        return new GlobalKey();
    }

    /**
     * Creates a <a href="https://api.flutter.dev/flutter/widgets/ObjectKey-class.html">local key</a> with the specified value.
     */
    public static @NonNull Key of(@Nullable Object @NonNull ... values) {
        Objects.requireNonNull(values);
        return new LocalKey(values);
    }

    @NonNull
    abstract Widget wrap(@NonNull Widget widget);

    // név kicsit fura, de Flutterben is így hívják
    final static class GlobalKey extends Key {

        final MutableObservable<Widget> content = MutableObservable.ofNullable();
        private WidgetState<GlobalKeyWidgetImpl> widgetState;

        @Override
        @NonNull Widget wrap(@NonNull Widget widget) {
            // TODO detektálni kéne, ha egy refresh cycle-n belül 2 eltérő widgetet is próbálnak belerakni?
            content.set(widget);
            return new GlobalKeyWidget();
        }

        // WidgetTree.findOrCreateWidgetState-ben special case-elve van ez a widget, hogyha ilyet
        // talál, akkor ignorálja a previous WidgetInstantiationt és a KeyWrappereket is
        final class GlobalKeyWidget extends Widget {

            WidgetState<?> replacement(WidgetTree tree) {
                if (widgetState == null)
                    widgetState = new WidgetState<>(new GlobalKeyWidgetImpl(), tree);
                else if (widgetState.tree != tree)
                    throw new RuntimeException(GlobalKey.class.getSimpleName() + " reused for different tree: " +
                            tree);
                return widgetState;
            }

            @Override
            protected Widget build() {
                throw new RuntimeException("should not reach here (GKW.b)");
            }
        }

        private final class GlobalKeyWidgetImpl extends Widget {

            @Override
            protected Widget build() {
                Widget w = content.get();
                if (w == null)
                    throw new RuntimeException(/* TODO "Content has been removed"*/);
                return w;
            }
        }
    }

    final static class LocalKey extends Key {

        private final @Nullable Object @NonNull [] values;

        public LocalKey(@Nullable Object @NonNull [] values) {
            this.values = values;
        }

        @Override
        @NonNull Widget wrap(@NonNull Widget widget) {
            return new LocalKeyWidget(this, widget);
        }
    }

    static class LocalKeyWidget extends Widget {

        private final LocalKey key;
        private final Widget widget;

        public LocalKeyWidget(LocalKey key, Widget widget) {
            this.key = key;
            this.widget = widget;
        }

        @Override
        protected Widget build() {
            throw new RuntimeException("TODO");
        }
    }

    // TODO nevek itt még Slotból ragadtak (meg SlotMapnél is)
    /**
     * A collections of {@linkplain Key#create() global keys} that are indexed by an int.
     * <p>
     * This is usually used when a widget needs to show a list of items: each row gets a different slot, so when some row
     * changes, only the changed row refreshed, not the container of all items.
     */
    public static final class SlotList {

        private final List<Key> slots = new ArrayList<>();

        // TODO itt is lehetne detektálni a dupla meghívásokat (lehet akár véletlen is,
        //      pl. TabbedPaneben van 2 db SlotList)
        public @NonNull List<? extends Widget> with(@NonNull List<? extends @NonNull Widget> widgets) {
            List<Widget> result = new ArrayList<>();

            int i = 0;
            for (Widget w : widgets) {
                Key s;
                if (slots.size() == i)
                    slots.add(s = Key.create());
                else
                    s = slots.get(i);
                i++;
                // TODO ha w null, akkor jobb NPE üzenet kéne
                result.add(w.withKey(s));
            }

            slots.subList(i, slots.size()).clear();

            return List.copyOf(result);
        }
    }

    /**
     * A collection of {@linkplain Key#create() global keys} that are indexed by an arbitrary typed key.
     * <p>
     * This is usually used when a widget needs to show a list of items: each item gets a different slot, so when some
     * row changes, only the changed row refreshed, not the container of all items.
     */
    public static final class SlotMap<K> {

        private final Map<K, Key> slots = new HashMap<>();

        public @NonNull Map<@NonNull K, ? extends @NonNull Widget> with(
                @NonNull Map<@NonNull K, ? extends @NonNull Widget> widgets) {
            HashMap<K, Widget> m = new HashMap<>(widgets);

            m.replaceAll((key, widget) ->
                    widget.withKey(slots.computeIfAbsent(key, __ -> Key.create())));
            slots.keySet().retainAll(widgets.keySet());

            return Collections.unmodifiableMap(m);
        }

        public @NonNull SequencedMap<K, ? extends Widget> with(
                @NonNull SequencedMap<K, ? extends Widget> widgets) {
            LinkedHashMap<K, Widget> m = new LinkedHashMap<>(widgets);

            m.replaceAll((key, widget) ->
                    widget.withKey(slots.computeIfAbsent(key, __ -> create())));
            slots.keySet().retainAll(widgets.keySet());

            return Collections.unmodifiableSequencedMap(m);
        }
    }
}
