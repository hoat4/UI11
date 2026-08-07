package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.observable.MutableObservable;
import ui11.provide.Provider;

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

        // ennek csak ezért azért nem lehet rögtön értéket adni, mert WidgetTree-t nem ismerjük
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

            @Override
            public String toString() {
                // TODO Widget.toString exceptionök?
                return "GlobalKeyWidget{key=" + GlobalKey.this + ", content=" + content.snoop() + "}";
            }
        }

        final class GlobalKeyWidgetImpl extends Widget {

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

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            LocalKey localKey = (LocalKey) o;
            return Arrays.equals(values, localKey.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }

        @Override
        public String toString() {
            return "LocalKey" + Arrays.toString(values);
        }

        static LocalKey findLocalKey(Widget widget) {
            // ez maradjon szinkronban WidgetTree.findOrCreateWidgetState-vel
            while (true) {
                switch (widget) {
                    case null -> {
                        throw new NullPointerException("findLocalKey null");
                    }
                    case Provider<?> p -> {
                        widget = p.content();
                        // ha InheritedValueMerger van, akkor is belemegyünk, mert az implementációs
                        // részletkérdés, hogy 
                    }
                    case Key.LocalKey.LocalKeyWidget localKeyWidget -> {
                        return localKeyWidget.key;
                    }
                    default -> {
                        return null;
                    }
                }
            }
        }


        // WidgetTree.findOrCreateWidgetState-ben special case-elve van ez a widget, hogyha ilyet
        // talál, akkor skippeli ezt és a tartalmazott widgetre ugrik
        static class LocalKeyWidget extends Widget {

            private final LocalKey key;
            final Widget widget;

            public LocalKeyWidget(LocalKey key, Widget widget) {
                this.key = key;
                this.widget = widget;
            }

            @Override
            protected Widget build() {
                throw new RuntimeException("should not reach here (LKW.b)");
            }
        }
    }

    // TODO nevek itt még Slotból ragadtak (meg SlotMapnél is)

    /**
     * A collections of {@linkplain Key#create() global keys} that are indexed by an int.
     * <p>
     * This is usually used when a widget needs to show a list of items: each row gets a different slot, so when some row
     * changes, only the changed row refreshed, not the container of all items.
     */
    public static final class ListKeyCache /* extends KeyCache<Integer>? */ {

        private final List<GlobalKey> globalKeysByIndex = new ArrayList<>();
        private final Map<LocalKey, GlobalKey> globalKeysByLocalKeys = new HashMap<>();

        public ListKeyCache() {
        }

        // TODO itt is lehetne detektálni a dupla meghívásokat (lehet akár véletlen is,
        //      pl. TabbedPaneben van 2 db ListKeyCache)
        public @NonNull List<? extends Widget> with(@NonNull List<? extends @NonNull Widget> widgets) {
            List<Widget> result = new ArrayList<>();
            Set<LocalKey> usedLocalKeys = new HashSet<>();

            int i = 0;
            for (Widget w : widgets) {
                // TODO ha w null, akkor jobb NPE üzenet kéne

                LocalKey localKey = LocalKey.findLocalKey(w);
                if (localKey != null) {
                    if (!usedLocalKeys.add(localKey))
                        throw new RuntimeException("Duplicate local key: " + localKey);
                    w = w.withKey(globalKeysByLocalKeys.computeIfAbsent(localKey, __ -> new GlobalKey()));
                }

                GlobalKey globalKey;
                if (globalKeysByIndex.size() == i)
                    globalKeysByIndex.add(globalKey = new GlobalKey());
                else
                    globalKey = globalKeysByIndex.get(i);
                i++;
                result.add(w.withKey(globalKey));
            }

            globalKeysByIndex.subList(i, globalKeysByIndex.size()).clear();
            globalKeysByLocalKeys.keySet().retainAll(usedLocalKeys);

            return List.copyOf(result);
        }
    }

    /**
     * A collection of {@linkplain Key#create() global keys} that are indexed by an arbitrary typed key.
     * <p>
     * This is usually used when a widget needs to show a list of items: each item gets a different slot, so when some
     * row changes, only the changed row refreshed, not the container of all items.
     */
    public static final class KeyCache<K> {

        private final Map<K, GlobalKey> globalKeysByIndex = new HashMap<>();
        private final Map<LocalKey, GlobalKey> globalKeysByLocalKeys = new HashMap<>();

        public KeyCache() {
        }

        public @NonNull Map<@NonNull K, ? extends @NonNull Widget> with(
                @NonNull Map<@NonNull K, ? extends @NonNull Widget> widgets) {
            HashMap<K, Widget> m = new HashMap<>(widgets);
            doUpdate(m);
            return Collections.unmodifiableMap(m);
        }

        public @NonNull SequencedMap<K, ? extends Widget> with(
                @NonNull SequencedMap<K, ? extends Widget> widgets) {
            LinkedHashMap<K, Widget> m = new LinkedHashMap<>(widgets);
            doUpdate(m);
            return Collections.unmodifiableSequencedMap(m);
        }

        private void doUpdate(HashMap<K, Widget> m) {
            Set<LocalKey> usedLocalKeys = new HashSet<>();

            m.replaceAll((key, widget) -> {
                // TODO ha w null, akkor jobb NPE üzenet kéne

                LocalKey localKey = LocalKey.findLocalKey(widget);
                if (localKey != null) {
                    if (!usedLocalKeys.add(localKey))
                        throw new RuntimeException("Duplicate local key: " + localKey);
                    widget = widget.withKey(globalKeysByLocalKeys.computeIfAbsent(localKey, __ -> new GlobalKey()));
                }

                final GlobalKey globalKey = globalKeysByIndex.computeIfAbsent(key, __ -> new GlobalKey());
                return widget.withKey(globalKey);
            });

            globalKeysByIndex.keySet().retainAll(m.keySet());
            globalKeysByLocalKeys.keySet().retainAll(usedLocalKeys);
        }

    }
}
