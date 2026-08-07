package ui11.text.formatted;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.layout.Gone;

import java.util.List;

public final class OrderedList extends SubstitutedWidget {

    private final @NonNull List<? extends @NonNull Widget> items;

    @Remember private Key.SlotList slots;

    public OrderedList(@NonNull List<? extends @Nullable Widget> items) {
        this.items = Gone.replaceNullsWithGone(items);
    }

    public OrderedList(@Nullable Widget @NonNull ... items) {
        this.items = Gone.replaceNullsWithGone(items);
    }

    @Override
    protected void initState() {
        slots = new Key.SlotList();
    }

    @Override
    protected OrderedList forSubstitution() {
        return new OrderedList(slots.with(items));
    }

    public @NonNull List<? extends @NonNull Widget> items() {
        return items;
    }

    // TODO default impl
}
