package ui11.graphics.effect;

import ui11.MultiSlot;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static ui11.graphics.Empty.empty;

// TODO valahol dokumentálni kéne, hogy ui-layout-constraints ad ehhez BoxLayoutProtocol implementációt.
//      meg ColorFillnél is.

/**
 * Shows multiple widgets on top of each other.
 * <p>
 * Despite the name, this class is not related to the color blending mode commonly called "overlay", actually it
 * composites using the Source-Over composite operation.
 * <p>
 * Painting order: The first widget in the list is shown behind others, the second one is shown in front of the first,
 * the third one is shown above the first and the second, and so on.
 */
public final class Overlay extends SubstitutedWidget {

    private final List<? extends Widget> items;

    @Remember private Slot2.SlotList slots;

    public Overlay(List<? extends Widget> items) {
        // TODO items = items.stream().map(Gone::goneIfNull).toList();
        //      de itt nem a layout-model modulban vagyunk, tehát nincsen Gone
        this.items = items.stream().
                map(w -> w == null ? empty() : w).
                collect(Collectors.toUnmodifiableList());
    }

    public static Overlay overlay(Widget... items) {
        return new Overlay(Arrays.asList(items));
    }

    public static Overlay overlay(Consumer<Consumer<Widget>> children) {
        List<Widget> w = new ArrayList<>();
        children.accept(w::add);
        return new Overlay(w);
    }

    @Override
    protected void initState() {
        slots = new Slot2.SlotList();
    }

    @Override
    protected Overlay forSubstitution() {
        return new Overlay(slots.with(items));
    }

    public List<? extends Widget> items() {
        return items;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + (items.isEmpty() ? " {}" : " {\n" +
                items.stream().
                        map(w -> "  " + w.toString().replace("\n", "\n  ")).
                        collect(joining(", \n"))
                + "\n}");
    }
}
