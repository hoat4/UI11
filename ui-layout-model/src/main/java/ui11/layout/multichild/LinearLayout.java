package ui11.layout.multichild;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.layout.Gap;
import ui11.layout.Gone;
import ui11.layout.Insets;
import ui11.layout.singlechild.Padding;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collectors.joining;
import static ui11.graphics.Empty.empty;

/**
 * Olyan konténer, ami vízszintesen vagy függőlegesen egymás mellé/alá elhelyezi a megadott elemeket.
 */
public final class LinearLayout extends SubstitutedWidget {

    private final Axis axis;
    private final List<? extends Widget> items;
    private final Length gap;

    public LinearLayout(Axis axis, List<? extends Widget> items) {
        this(axis, items, Length.zero());
    }

    public LinearLayout(Axis axis, Widget... items) {
        this(axis, Arrays.asList(items));
    }

    public LinearLayout(Axis axis, List<? extends Widget> items, Length gap) {
        Objects.requireNonNull(axis);
        Objects.requireNonNull(gap);
        // TODO stream helyett manuálisan
        items = items.stream().
                map(Gone::goneIfNull).
                toList();
        this.axis = axis;
        this.items = items;
        this.gap = gap;
    }

    public static LinearLayout row(Widget... elements) {
        return new LinearLayout(Axis.HORIZONTAL, Arrays.asList(elements));
    }

    public static LinearLayout column(Widget... elements) {
        return new LinearLayout(Axis.VERTICAL, Arrays.asList(elements));
    }

    public static LinearLayout row(Consumer<Builder> consumer) {
        Builder b = new Builder(Axis.HORIZONTAL);
        consumer.accept(b);
        return b.build();
    }

    // TODO ez mindenképpen azonnal hívja meg a consumert, vagy lehet lazy is?
    public static LinearLayout column(Consumer<Builder> consumer) {
        Builder b = new Builder(Axis.VERTICAL);
        consumer.accept(b);
        return b.build();
    }

    public static Builder rowBuilder() {
        return new Builder(Axis.HORIZONTAL);
    }

    public static Builder columnBuilder() {
        return new Builder(Axis.VERTICAL);
    }

    public static LinearLayout row(List<? extends Widget> elements) {
        return new LinearLayout(Axis.HORIZONTAL, elements);
    }

    public static LinearLayout column(List<? extends Widget> elements) {
        return new LinearLayout(Axis.VERTICAL, elements);
    }

    public static <W extends Widget> Collector<W, ?, LinearLayout> toRow() {
        return new LinearLayoutCollector<>(Axis.HORIZONTAL, Length.zero());
    }

    public static <W extends Widget> Collector<W, ?, LinearLayout> toRow(Length gap) {
        return new LinearLayoutCollector<>(Axis.HORIZONTAL, gap);
    }

    public static <W extends Widget> Collector<W, ?, LinearLayout> toColumn() {
        return new LinearLayoutCollector<>(Axis.VERTICAL, Length.zero());
    }

    public static <W extends Widget> Collector<W, ?, LinearLayout> toColumn(Length gap) {
        return new LinearLayoutCollector<>(Axis.VERTICAL, gap);
    }

    public static Item withWeight(double weight, Widget e) {
        if (e == null)
            return null;
        return new Item(weight, e);
    }

    public static Item expanded(Widget e) {
        return withWeight(1, e);
    }

    public Axis axis() {
        return axis;
    }

    public List<? extends Widget> items() {
        return items;
    }

    public Length gap() {
        return gap;
    }

    /**
     * A megadott hossz lesz az elemek közti egyenkénti helyköz is, és az elemeket körülvevő szegély is.
     */
    public Widget withPadAndGap(Length padAndGap) {
        if (padAndGap.isZero())
            return this;

        return new Padding(
                Insets.all(padAndGap),
                withGap(padAndGap)
        );
    }

    /**
     * Az elemek közötti rést beállítja.
     */
    public LinearLayout withGap(Length gap) {
        // TODO ilyenkor meg kéne előzni items deep copyját
        return new LinearLayout(axis, items, gap);
    }

    public LinearLayout alignChildrenCenter() {
        // TODO erre inkább egy property kéne (mint CSS-ben a align-items),
        //      az gyorsabb lenne mint új listát gyárta amiben wrappeljük Alignnal
        //      meg most így össze is kavarodik könnyen weight-tal
        Alignment alignment = switch (axis) {
            case VERTICAL -> Alignment.HCENTER;
            case HORIZONTAL -> Alignment.VCENTER;
        };
        return new LinearLayout(axis, items.stream().
                map(w -> withWeight(Item.weight(w), Align.align(alignment, w))).
                toList(), gap);
    }

    public LinearLayout distributeSpaceEvenly() {
        // TODO ld. komment alignChildrenCenterben
        if (items.stream().anyMatch(w -> Item.weight(w) != 0))
            throw new IllegalStateException();

        if (items.isEmpty())
            return this;

        List<Widget> l = new ArrayList<>();
        l.add(new Item(1, empty()));
        for (Widget w : items) {
            l.add(w);
            l.add(new Item(1, empty()));
        }
        return new LinearLayout(axis, l, gap);
    }

    public LinearLayout distributeSpaceAround() {
        // TODO ld. komment alignChildrenCenterben
        if (items.stream().anyMatch(w -> Item.weight(w) != 0))
            throw new IllegalStateException();

        if (items.isEmpty())
            return this;

        List<Widget> l = new ArrayList<>();
        for (Widget w : items) {
            l.add(new Item(1, empty()));
            l.add(w);
            l.add(new Item(1, empty()));
        }
        return new LinearLayout(axis, l, gap);
    }

    public Widget distributeSpaceBetween() {
        // TODO ld. komment alignChildrenCenterben
        if (items.stream().anyMatch(w -> Item.weight(w) != 0))
            throw new IllegalStateException();

        if (items.size() <= 1)
            return this;

        List<Widget> l = new ArrayList<>();
        Iterator<? extends Widget> iterator = items.iterator();

        l.add(iterator.next());
        while (iterator.hasNext()) {
            Widget w = iterator.next();
            l.add(new Item(1, empty()));
            l.add(w);
        }
        return new LinearLayout(axis, l, gap);
    }

    public LinearLayout reversedIf(boolean reverse) {
        if (!reverse)
            return this;

        List<Widget> l = new ArrayList<>(items);
        Collections.reverse(l);
        return new LinearLayout(axis, l);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + axis + (items.isEmpty() ? " {}" : " {\n" +
                items.stream().
                        map(w -> "  " + w.toString().replace("\n", "\n  ")).
                        collect(joining(", \n"))
                + "\n}");
    }

    public static class Builder {

        private final Axis axis;
        private final List<Widget> items = new ArrayList<>();
        private Length gap = Length.zero();

        public Builder(Axis axis) {
            this.axis = axis;
        }

        public Builder add(Widget w) {
            items.add(w);
            return this;
        }

        public Builder add(double weight, Widget w) {
            items.add(withWeight(weight, w));
            return this;
        }

        public Builder gap(Length gap) {
            items.add(new Gap(axis, gap));
            return this;
        }

        public Builder expanded(Widget w) {
            items.add(LinearLayout.expanded(w));
            return this;
        }

        public Builder overallGap(Length gap) {
            this.gap = gap;
            return this;
        }

        public LinearLayout build() {
            return new LinearLayout(axis, items, gap);
        }
    }

    public static final class Item extends Widget {
        private final double weight;
        private final Widget content;

        public Item(double weight, Widget content) {
            Objects.requireNonNull(content);
            if (weight < 0 || !Double.isFinite(weight))
                throw new IllegalArgumentException("invalid weight: " + weight + " for " + content);
            this.weight = weight;
            this.content = content;
        }

        public static double weight(Widget e) {
            // TODO ehelyett UpValue rendszert kéne használni
            return e instanceof Item item ? item.weight : 0;
        }

        @Override
        protected Widget build() {
            return content;
        }
    }

    private static class LinearLayoutCollector<W extends Widget> implements Collector<W, List<W>, LinearLayout> {

        private final Axis axis;
        private final Length gap;

        private LinearLayoutCollector(Axis axis, Length gap) {
            this.axis = axis;
            this.gap = gap;
        }

        @Override
        public Supplier<List<W>> supplier() {
            return ArrayList::new; // vagy SpinedBuffer-szerűség?
        }

        @Override
        public BiConsumer<List<W>, W> accumulator() {
            return List::add;
        }

        @Override
        public BinaryOperator<List<W>> combiner() {
            return (a, b) -> {
                a.addAll(b);
                return a;
            };
        }

        @Override
        public Function<List<W>, LinearLayout> finisher() {
            return l -> new LinearLayout(axis, l).withGap(gap);
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of();
        }
    }
}
