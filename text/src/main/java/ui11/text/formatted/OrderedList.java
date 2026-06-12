package ui11.text.formatted;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.layout.Gone;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class OrderedList extends SubstitutedWidget {

    private final List<? extends Widget> items;

    public OrderedList(List<? extends Widget> items) {
        this.items = List.copyOf(items);
    }

    public OrderedList(Widget... items) {
        // TODO itt .collect(toUnmodifiableList()) van, míg LinearLayoutban .toList()
        this(Arrays.stream(items).map(Gone::goneIfNull).collect(Collectors.toUnmodifiableList()));
    }

    public List<? extends Widget> items() {
        return items;
    }

    // TODO default impl
}
