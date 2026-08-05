package ui11.control;

import ui11.*;
import ui11.layout.HorizontalAlignment;
import ui11.text.Text;

import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ui11.graphics.Empty.empty;
import static ui11.text.TextModifiers.withLineWrapping;

public class Table<T> extends SubstitutedWidget {

    private final List<? extends T> rows;
    private final List<? extends Column<? super T>> columns;
    private final Consumer<T> clickHandler;

    @Remember private Map<CellKey<T>, Slot2> cellSlots;

    public Table(List<? extends Column<? super T>> columns, List<? extends T> rows) {
        this(columns, rows, null);
    }

    public Table(List<? extends Column<? super T>> columns, List<? extends T> rows, Consumer<T> clickHandler) {
        this.rows = rows;
        this.columns = columns;
        this.clickHandler = listenerProxy(clickHandler);
    }

    @Override
    protected void initState() {
        cellSlots = new HashMap<>();
        // TODO ennek a mapnek a törlése
    }

    @Override
    protected Table<?> forSubstitution() {
        return new Table<>(
                columns.stream().
                        map(col -> col.wrapContentInSlot(cellSlots)).
                        toList(),
                rows,
                clickHandler
        );
    }

    public List<? extends T> rows() {
        return rows;
    }

    public List<? extends Column<? super T>> columns() {
        return columns;
    }

    public Consumer<T> clickHandler() {
        return clickHandler;
    }

    private record CellKey<T>(T row, Column<? super T> column) {
    }

    // identity equals, hogy CellKey ne csak akkor működjön ha a title meg align meg cellContentFunction közül
    // legalább egy eltér
    public static final class Column<T> {

        private final @NonNull String title;
        private final @NonNull HorizontalAlignment horizontalAlignment;
        private final @NonNull Function<T, @NonNull Widget> cellContentFunction;

        public Column(@NonNull String title,
                      @NonNull HorizontalAlignment horizontalAlignment,
                      @NonNull Function<T, @NonNull Widget> cellContentFunction) {
            Objects.requireNonNull(title);
            Objects.requireNonNull(horizontalAlignment);
            Objects.requireNonNull(cellContentFunction);
            this.title = title;
            this.horizontalAlignment = horizontalAlignment;
            this.cellContentFunction = cellContentFunction;
        }

        public @NonNull String title() {
            return title;
        }

        public @NonNull HorizontalAlignment horizontalAlignment() {
            return horizontalAlignment;
        }

        public @NonNull Widget cellContent(T row) {
            Widget result = cellContentFunction.apply(row);
            Objects.requireNonNull(result); // TODO msg
            return result;
        }

        <T2 extends T> Column<T2> wrapContentInSlot(Map<CellKey<T2>, Slot2> cellSlots) {
            return new Column<>(
                    title,
                    horizontalAlignment,
                    row -> cellSlots.computeIfAbsent(new CellKey<>(row, this), __ -> new Slot2()).
                            with(cellContent(row))
            );
        }

        @Override
        public String toString() {
            return "Column[" +
                    "title=" + title + ", " +
                    "horizontalAlignment=" + horizontalAlignment + ", " +
                    "cellContentFunction=" + cellContentFunction + ']';
        }
    }

    public static <T> Builder<T> ofData(List<T> rows) {
        return new Builder<>(rows);
    }


    public static class Builder<T> {

        private final List<T> rows;
        private final List<Column<T>> cols = new ArrayList<>();
        private Consumer<T> clickHandler;

        private Builder(List<T> rows) {
            this.rows = rows;
        }

        public Table.Builder<T> stringCol(String title, Function<T, String> function) {
            cols.add(new Column<>(title, HorizontalAlignment.LEFT,
                    e -> {
                        String s = function.apply(e);
                        return s == null ? empty() : withLineWrapping(new Text(s));
                    }));
            return this;
        }

        public Table.Builder<T> intCol(String title, Function<T, Integer> function) {
            cols.add(new Column<>(title, HorizontalAlignment.RIGHT,
                    e -> {
                        String s = Integer.toString(function.apply(e));
                        return withLineWrapping(new Text(s));
                    }));
            return this;
        }

        public Builder<T> onClick(Consumer<T> clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        public Table<T> build() {
            return new Table<>(cols, rows, clickHandler);
        }
    }
}
