package ui11.control;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.layout.HorizontalAlignment;
import ui11.text.Text;

import org.jspecify.annotations.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static ui11.graphics.Empty.empty;
import static ui11.text.TextModifiers.withLineWrapping;

public class Table<T> extends SubstitutedWidget {

    private final List<? extends T> rows;
    private final List<? extends Column<? super T>> columns;
    private final Consumer<T> clickHandler;

    public Table(List<? extends Column<? super T>> columns, List<? extends T> rows) {
        this(columns, rows, null);
    }

    public Table(List<? extends Column<? super T>> columns, List<? extends T> rows, Consumer<T> clickHandler) {
        this.rows = rows;
        this.columns = columns;
        this.clickHandler = listenerProxy(clickHandler);
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

    // cellContentFunction jelenleg nem adhat vissza nullt
    public record Column<T>(@NonNull String title, @NonNull HorizontalAlignment horizontalAlignment,
                            @NonNull Function<T, Widget> cellContentFunction) {

        public Column {
            Objects.requireNonNull(title);
            Objects.requireNonNull(horizontalAlignment);
            Objects.requireNonNull(cellContentFunction);
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
