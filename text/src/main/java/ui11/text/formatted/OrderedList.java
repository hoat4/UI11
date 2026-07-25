package ui11.text.formatted;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.MultiSlot;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.layout.Gone;

import java.util.List;

public final class OrderedList extends SubstitutedWidget {

    private final @NonNull List<? extends @NonNull Widget> items;

    @Inject private MultiSlot<Integer> slots;

    public OrderedList(@NonNull List<? extends @Nullable Widget> items) {
        this.items = Gone.replaceNullsWithGone(items);
    }

    public OrderedList(@Nullable Widget @NonNull ... items) {
        this.items = Gone.replaceNullsWithGone(items);
    }

    public @NonNull List<? extends @NonNull Widget> items() {
        return slots == null ? items : MultiSlot.assignSlots(slots, items);
    }

    // TODO default impl
}
