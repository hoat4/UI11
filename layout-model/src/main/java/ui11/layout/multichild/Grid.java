package ui11.layout.multichild;

import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Axis;
import ui11.geom.Length;
import ui11.layout.singlechild.Align;
import ui11.layout.singlechild.Alignment;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;
import static ui11.geom.Length.zero;

public class Grid extends SubstitutedWidget {

    private final List<Item> items;
    private final Map<Integer, TrackSettings> columnSettings;
    private final Map<Integer, TrackSettings> rowSettings;

    private final Length gap;
    private final boolean ignorePrefSizes;

    // TODO ki kéne találni, hogy hogyan lehet enélkül passiveHeight-ot megcsinálni
    private final Axis orientationBias;

    @Remember private Slot.SlotMap<GridItemKey> slots;

    private Grid(Builder b) {
        items = b.items;
        columnSettings = b.columnSettings;
        rowSettings = b.rowSettings;
        gap = b.gap;
        ignorePrefSizes = b.ignorePrefSizes;
        orientationBias = b.orientationBias;
    }

    // forSubstitution()-nek, ideiglenesen
    private Grid(List<Item> items,
                 Map<Integer, TrackSettings> columnSettings, Map<Integer, TrackSettings> rowSettings,
                 Length gap, boolean ignorePrefSizes, Axis orientationBias) {
        this.items = items;
        this.columnSettings = columnSettings;
        this.rowSettings = rowSettings;
        this.gap = gap;
        this.ignorePrefSizes = ignorePrefSizes;
        this.orientationBias = orientationBias;
    }

    public static Builder builder() {
        return new Builder(Integer.MAX_VALUE);
    }

    public static Builder builder(int autoWrap) {
        return new Builder(autoWrap);
    }

    public static Grid grid(Consumer<Builder> gridBuilderConsumer) {
        Builder b = builder();
        gridBuilderConsumer.accept(b);
        return b.build();
    }

    public static Grid grid(int autoWrap, Consumer<Builder> gridBuilderConsumer) {
        Builder b = builder(autoWrap);
        gridBuilderConsumer.accept(b);
        return b.build();
    }

    /**
     * @param elements identity equalst néz ebben
     */
    public static Grid of(int cols, Widget... elements) {
        // TODO ha Gone van elementsben, akkor mi legyen?

        Builder builder = builder(cols);
        if (cols == -1)
            cols = elements.length;
        for (int i = 0; i < elements.length; i++) {
            Widget e = elements[i];
            if (e == null) {
                builder.skip(1);
                builder.items.add(null);
            } else {
                if (i >= cols && elements[i - cols] == e) {
                    if (i % cols == 0 || elements[i - 1] != e) {
                        int j = i - cols;
                        Item ge;
                        while ((ge = builder.items.get(j)) == null)
                            j -= cols;
                        builder.items.set(j, new Item(ge.widget(), ge.col(), ge.row(), ge.colspan(), ge.rowspan()));
                        builder.skip(1);
                    }
                    builder.items.add(null);
                } else {
                    int colspan = 1;
                    while (i + colspan < elements.length && elements[i + colspan] == e)
                        colspan++;
                    builder.add(e, colspan, 1);
                    i += colspan - 1;
                    while (colspan != 1) {
                        builder.items.add(null);
                        colspan--;
                    }
                }
            }
            assert builder.items.size() == i + 1 : i + ", " + builder.items;
        }
        builder.items.removeIf(Objects::isNull);
        return builder.build();
    }

    @Override
    protected void initState() {
        slots = new Slot.SlotMap<>();
    }

    @Override
    protected Grid forSubstitution() {
        // TODO ez a kód most duplikálva van itt és DOMGridPeerben
        record GridPos(int col, int row) {
        }
        Map<GridPos, Integer> overlayCounts = new HashMap<>();
        Map<GridItemKey, Widget> widgetsByGridItemKey = new HashMap<>();
        GridItemKey[] keys = new GridItemKey[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            int overlayIndex = overlayCounts.compute(new GridPos(item.col(), item.row()),
                    (p, j) -> j == null ? 0 : j + 1);
            keys[i] = new GridItemKey(item.col(), item.row(), overlayIndex);
            widgetsByGridItemKey.put(keys[i], item.widget);
        }

        Item[] itemsArray = new Item[this.items.size()];
        Map<GridItemKey, ? extends Widget> slottedWidgetsByGridItemKey = slots.with(widgetsByGridItemKey);
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            Widget slottedW = slottedWidgetsByGridItemKey.get(keys[i]);
            assert slottedW != null;
            itemsArray[i] = new Item(slottedW,
                    item.col, item.row, item.colspan, item.rowspan);
        }

        List<Item> itemList = List.of(itemsArray);
        return new Grid(itemList, columnSettings, rowSettings, gap, ignorePrefSizes, orientationBias);
    }

    public List<Item> items() {
        return items;
    }

    public Map<Integer, TrackSettings> tracks(@NonNull Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> columnSettings;
            case VERTICAL -> rowSettings;
        };
    }

    public Length gap() {
        return gap;
    }

    public Axis orientationBias() {
        return orientationBias;
    }

    public boolean ignorePrefSizes() {
        return ignorePrefSizes;
    }

    public @NonNull TrackSettings column(int i) {
        return columnSettings.getOrDefault(i, TrackSettings.DEFAULT);
    }

    public @NonNull TrackSettings row(int i) {
        return rowSettings.getOrDefault(i, TrackSettings.DEFAULT);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + (items.isEmpty() ? " {}" : " {\n" +
                items.stream().
                        map(item ->
                                "  (" + item.col() + ", " + item.row() + " " +
                                        item.rowspan() + "×" + item.colspan() + ") " +
                                        item.widget().toString().replace("\n", "\n " + " ")).
                        collect(joining(", \n"))
                + "\n}");
    }

    private record GridItemKey(int col, int row, int overlayIndex) {
    }

    public static final record Item(@NonNull Widget widget, int col, int row,
                                    int colspan, int rowspan) {

        public Item {
            if (col < 0 || row < 0)
                throw new RuntimeException("negative column or row number: " + widget + ", " +
                        col + ", " + row + ", " + colspan + ", " + rowspan);
            if (colspan <= 0 || rowspan <= 0)
                throw new RuntimeException("non-positive column or row span: " + widget + ", " +
                        col + ", " + row + ", " + colspan + ", " + rowspan);
            Objects.requireNonNull(widget);
        }

        public Item(Widget element, int col, int row) {
            this(element, col, row, 1, 1);
        }
    }

    public static final record TrackSettings(double weight, Optional<Length> size) {

        public TrackSettings {
            if (weight < 0 || !Double.isFinite(weight))
                throw new IllegalArgumentException("weight must be non negative: " + weight);
        }

        static final TrackSettings DEFAULT = new TrackSettings(0, Optional.empty());
    }

    public static final record InsetWeights(double top, double right, double bottom, double left) {

        public static final InsetWeights ZERO = new InsetWeights(0, 0, 0, 0);

        public double sum(Axis axis) {
            return switch (axis) {
                case HORIZONTAL -> left + right;
                case VERTICAL -> top + bottom;
            };
        }

        public double begin(Axis axis) {
            return switch (axis) {
                case HORIZONTAL -> left;
                case VERTICAL -> top;
            };
        }

        public double end(Axis axis) {
            return switch (axis) {
                case HORIZONTAL -> right;
                case VERTICAL -> bottom;
            };
        }
    }

    public static class Builder {

        private final List<Item> items = new ArrayList<>();

        public int cursorX, cursorY;
        private final int autoWrap;
        private @NonNull Length gap = zero();

        private final Map<Integer, TrackSettings> columnSettings = new HashMap<>();
        private final Map<Integer, TrackSettings> rowSettings = new HashMap<>();

        private boolean ignorePrefSizes;

        // TODO ki kéne találni, hogy hogyan lehet enélkül passiveHeight-ot megcsinálni
        private @NonNull Axis orientationBias = Axis.HORIZONTAL;

        private boolean finished;

        private Builder(int autoWrap) {
            if (autoWrap <= 0)
                throw new IllegalArgumentException("cols=" + autoWrap);
            this.autoWrap = autoWrap;
        }

        public Builder setGap(@NonNull Length gap) {
            Objects.requireNonNull(gap);
            ensureNotFinished();
            this.gap = gap;
            return this;
        }

        public Builder add(@Nullable Widget e) {
            ensureNotFinished();
            if (e == null)
                return this;

            items.add(new Item(e, cursorX, cursorY, 1, 1));
            cursorX++;
            if (cursorX == autoWrap)
                newline();
            return this;
        }

        public Builder add(@Nullable Widget e, int colspan, int rowspan) {
            ensureNotFinished();
            if (e == null)
                return this;

            checkSkip(colspan);
            items.add(new Item(e, cursorX, cursorY, colspan, rowspan));
            doSkip(colspan);
            return this;
        }

        public Builder skip(int cols) {
            ensureNotFinished();
            checkSkip(cols);
            doSkip(cols);
            return this;
        }

        private void checkSkip(int colspan) {
            if (cursorX < autoWrap && cursorX + colspan > autoWrap)
                throw new IllegalArgumentException(cursorX + ", " + colspan + ", " + autoWrap);
        }

        private void doSkip(int colspan) {
            ensureNotFinished();
            cursorX += colspan;
            if (cursorX == autoWrap)
                newline();
        }

        public Builder newline() {
            ensureNotFinished();
            cursorX = 0;
            cursorY++; // lehet hogy nem 1-el, hanem a sorbeli min rowspannal kéne növelni
            return this;
        }

        public Item element(Object e) {
            ensureNotFinished();
            return items.stream().filter(ge -> ge.widget() == e).findAny().orElseThrow();
        }

        public Builder background(Widget element) {
            ensureNotFinished();

            partialBackground(element, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            return this;
        }

        public Builder partialBackground(Widget element, int col, int row) {
            ensureNotFinished();

            partialBackground(element, col, row, 1, 1);
            return this;
        }

        public Builder partialBackground(Widget element, int col, int row, int colspan, int rowspan) {
            ensureNotFinished();

            items.add(0, new Item(element, col, row, colspan, rowspan));
            return this;
        }

        public GridArea cell(int col, int row) {
            return new GridArea(col, row, 1, 1);
        }

        public GridArea area(int col, int row, int colspan, int rowspan) {
            return new GridArea(col, row, colspan, rowspan);
        }

    /*
    public GridArea whole() {
        return new GridArea(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
     */

        public Builder columns(TrackSettings... columns) {
            ensureNotFinished();

            this.columnSettings.clear();
            for (int i = 0; i < columns.length; i++)
                this.columnSettings.put(i, columns[i]);
            return this;
        }

        public Builder rows(TrackSettings... rows) {
            ensureNotFinished();

            this.rowSettings.clear();
            for (int i = 0; i < rows.length; i++)
                this.rowSettings.put(i, rows[i]);
            return this;
        }

        public Builder rowWeights(double... rowWeights) {
            ensureNotFinished();

            for (int i = 0; i < rowWeights.length; i++) {
                final double weight = rowWeights[i];
                setRowWeight(i, weight);
            }
            return this;
        }

        public Builder columnWeights(double... columnWeights) {
            ensureNotFinished();

            for (int i = 0; i < columnWeights.length; i++) {
                setColumnWeight(i, columnWeights[i]);
            }
            return this;
        }

        /**
         * @param i      0-tól kezdődik a számozása
         * @param weight
         */
        public void setRowWeight(int i, double weight) {
            ensureNotFinished();

            TrackSettings prev = rowSettings.getOrDefault(i, TrackSettings.DEFAULT);
            this.rowSettings.put(i, new TrackSettings(weight, prev.size()));
        }

        /**
         * @param i      0-tól kezdődik a számozása
         * @param weight
         */
        public void setColumnWeight(int i, double weight) {
            ensureNotFinished();

            TrackSettings prev = columnSettings.getOrDefault(i, TrackSettings.DEFAULT);
            this.columnSettings.put(i, new TrackSettings(weight, prev.size()));
        }

        public Grid build() {
            ensureNotFinished();
            finished = true;
            return new Grid(this);
        }

        private void ensureNotFinished() {
            if (finished)
                throw new IllegalStateException();
        }

        public class GridArea {

            private final int col, row, colspan, rowspan;

            private final List<Item> alreadyAdded = new ArrayList<>();
            private Alignment alignment;

            public GridArea(int col, int row, int colspan, int rowspan) {
                this.col = col;
                this.row = row;
                this.colspan = colspan;
                this.rowspan = rowspan;
            }

            public GridArea background(@NonNull Widget elem) {
                Item ge = new Item(elem, col, row, colspan, rowspan);
                // ge.z = -1;
                items.add(ge);
                return this;
            }

            public GridArea add(@NonNull Widget elem) {
                if (alignment != null)
                    elem = Align.align(alignment, elem);
                Item e = new Item(elem, col, row, colspan, rowspan);
                alreadyAdded.add(e);
                items.add(e);
                return this;
            }

            /*
            public GridArea align(Alignment alignment) {
                for (Item ge : alreadyAdded) {
                    ge.e = Align.align(alignment, ge.e);
                }
                this.alignment = alignment;
                return this;
            }
             */
        }
    }
}
