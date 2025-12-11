package ui11.input.keyboard;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * Billentyűkombináció lenyomásáról szóló esemény. Nyomva tartás esetén az automatikus ismétlés miatt újra és újra
 * meghívódik ez az esemény.
 */
// TODO ez nincs még egy platformon se támogatva
public final class KeyTypeListener extends SubstitutedWidget {

    @Listener private final Consumer<KeyCombination> consumer;
    private final Widget content;

    public KeyTypeListener(Consumer<KeyCombination> consumer, Widget content) {
        // a consumer lehetne nemnull, mert különben nincs értelme
        this.consumer = consumer;
        this.content = content;
    }

    public Consumer<KeyCombination> consumer() {
        return consumer;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return content;
    }
}
