package ui11;

import org.jspecify.annotations.Nullable;
import ui11.provide.DynamicProvider;
import ui11.provide.Provider;
import ui11.provide.Provider.Mergeable;

import java.util.Objects;

class InheritedValueMerger<T> extends Widget {

    private final Class<T> type;
    private final T newValue;
    private final Widget content;

    InheritedValueMerger(Provider<T> p) {
        this.type = p.type();
        this.newValue = p.value();
        this.content = p.content();
    }

    @Override
    protected Widget build() {
        if (newValue == null)
            return content;

        WidgetState<?> widgetState = widgetState();
        Object prevVal = widgetState.tree.getIVForCurrentWidget(widgetState, type, true);
        T val;

        if (prevVal != WidgetTree.IV_NOT_PROVIDED) {
            // DynamicProvider "kvázi-mergeable"
            if (type == DynamicProvider.class)
                val = type.cast(mergeDynamicProviders((DynamicProvider) prevVal, (DynamicProvider) newValue));
            else {
                @SuppressWarnings("unchecked")
                Object merged = ((Mergeable<Object>) newValue).mergeWith(prevVal);
                if (merged == null || merged.getClass() != newValue.getClass())
                    // azért nem engedjük, mert findIVProvidesUntil belezavarodna, hogy
                    // az ivsből vagy a mergeableIVsből szedje.
                    // ha ezt mégis engedjük, akkor is annyit legalább kéne ellenőrizni,
                    // hogy p.type().isInstance(val) (vagy null)
                    throw new RuntimeException("Mergeable returned with different type: " + newValue + ", " +
                            prevVal + " -> " + merged +
                            " (" +
                            newValue.getClass().getName() + ", " +
                            (prevVal == null ? "null" : prevVal.getClass().getName()) + " -> " +
                            (merged == null ? "null" : merged.getClass().getName())
                            + ")");

                val = type.cast(merged);
            }
        } else
            val = newValue;

        // returnedProviderShouldNotBeMerged==true miatt ez mert nem lesz IVM-mé átalakítva
        return new Provider<>(type, val, content);
    }

    boolean returnedProviderShouldNotBeMerged() {
        return newValue != null;
    }

    private DynamicProvider mergeDynamicProviders(DynamicProvider prev, DynamicProvider value) {
        Objects.requireNonNull(prev);
        Objects.requireNonNull(value);

        // TODO ha valamelyik már mergeölt, akkor nem kéne újat csinálni, mert SOE lehet ha túl sok van belőlük
        return new DynamicProvider() {
            @Override
            public <T> @Nullable T provideOrNull(Class<T> type) {
                T t = value.provideOrNull(type);
                return t != null ? t : prev.provideOrNull(type);
            }

            @Override
            public String toString() {
                return "Merged " + DynamicProvider.class.getSimpleName() + "s {prev=" + prev + ", new=" + value + "}";
            }
        };
    }
}
