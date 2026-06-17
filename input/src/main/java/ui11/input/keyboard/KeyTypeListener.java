package ui11.input.keyboard;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Billentyűkombináció lenyomásáról szóló esemény. Nyomva tartás esetén az automatikus ismétlés miatt újra és újra
 * meghívódik ez az esemény.
 */
// TODO ez nincs még egy platformon se támogatva
public final class KeyTypeListener extends SubstitutedWidget {

    private final Consumer<KeyCombination> consumer;
    private final Widget content;

    public KeyTypeListener(@NonNull Consumer<KeyCombination> consumer, @NonNull Widget content) {
        this.consumer = listenerProxy(Objects.requireNonNull(consumer));
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Consumer<KeyCombination> consumer() {
        return consumer;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }
}
