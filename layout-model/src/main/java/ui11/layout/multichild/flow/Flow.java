package ui11.layout.multichild.flow;

import org.jspecify.annotations.Nullable;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import ui11.layout.Gone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

// TODO align állítása. talán TextAlign típussal, csak az most elérhetetlen, hogy külön modulba került.

// vagy Flow helyett "Wrap"? vagy lehet akár LinearLayoutnak is egy propertyje

public final class Flow extends SubstitutedWidget {

    private final @NonNull List<? extends @NonNull Widget> items;

    @Remember private Key.ListKeyCache slots;

    public Flow(@NonNull List<? extends @Nullable Widget> items) {
        this.items = Gone.replaceNullsWithGone(items);
    }

    @Override
    protected void initState() {
        slots = new Key.ListKeyCache();
    }

    @Override
    protected Flow forSubstitution() {
        return new Flow(slots.with(items));
    }

    public @NonNull List<? extends @NonNull Widget> items() {
        return items;
    }

    public static Flow flow(@Nullable Widget @NonNull ... elements) {
        return new Flow(Arrays.asList(elements));
    }

    public static Flow flow(@NonNull Consumer<@NonNull Consumer<@Nullable Widget>> elements) {
        List<Widget> l = new ArrayList<>();
        elements.accept(l::add);
        return new Flow(l);
    }

    public static <T extends Widget> Collector<T, ?, Flow> toFlow() {
        return Collectors.filtering(Objects::nonNull,
                Collectors.collectingAndThen(Collectors.toUnmodifiableList(), Flow::new));
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
