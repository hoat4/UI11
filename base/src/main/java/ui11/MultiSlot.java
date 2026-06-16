package ui11;

import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// TODO duplicate-ek detektálása

/**
 * A collections of {@link Slot Slots} that are cached by a key.
 * <p>
 * This is usually used when a widget needs to show a list of items: each row gets a different slot, so when some row
 * changes, only the changed row refreshed, not all items.
 *
 * @param <K> the type of the cache keys
 */
public final class MultiSlot<K> {

    /**
     * csak scope ellenőrzéshez kell
     */
    private final WidgetState<?> ownerWidgetState;

    // TODO ez így mem leak. mikor kéne törölni az elemeket belőle?
    private final Map<K, Slot> slots = new HashMap<>();

    MultiSlot(@NonNull WidgetState<?> ownerWidgetState) {
        this.ownerWidgetState = Objects.requireNonNull(ownerWidgetState);
    }

    // item nullable legyen?

    // 2025-11-08-ból megjegyzés keyekről (akkor még Elemenben voltak):
    //      withKey dobjon exceptiont ha duplicate
    //      De ez nem olyan egyszerű. Mit tekintünk duplicate-nek?
    //      - ha többször hívják meg ugyanazzal a kulccsal. De mi van, ha refreshSelf-en kívül van meghívva?
    //      - többször van felhasználva a KeyWrapper. Ez se jó, mert ez lehet legális is:
    //        pl. MultiChildLayoutImpl első alkalommal instantiateeli mérésre, második alkalommal meg berakja
    //        Overlay/Transform-ba childnak.

    public Slot get(K item) {
        Objects.requireNonNull(item);
        Slot result = slots.get(item);
        if (result == null)
            slots.put(item, result = new Slot(ownerWidgetState));
        return result;
    }

    public static List<? extends Widget> assignSlots(MultiSlot<Integer> slots, List<? extends Widget> items) {
        return IntStream.range(0, items.size()).
                mapToObj(i -> items.get(i).withSlot(slots.get(i))).
                collect(Collectors.toUnmodifiableList());
    }
}
