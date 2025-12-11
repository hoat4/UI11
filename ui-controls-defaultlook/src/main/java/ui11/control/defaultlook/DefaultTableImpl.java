package ui11.control.defaultlook;

import ui11.MultiSlot;
import ui11.Widget;
import ui11.control.Table;
import ui11.control.Table.Column;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.input.gesture.ClickListener;
import ui11.input.pointer.PointerStateDependent;
import ui11.layout.singlechild.Align;
import ui11.text.Text;

import java.util.Collection;
import java.util.Objects;

import static ui11.layout.multichild.Grid.grid;

public class DefaultTableImpl<T> extends Widget {

    private final Table<T> table;

    // TODO mi legyen, ha vannak duplikált sorok a táblázatban? és ha nullok?
    @Inject private MultiSlot<T> rowClickHandlerSlots;
    // kéne slot a sima celláknak is

    public DefaultTableImpl(Table<T> table) {
        this.table = table;
    }

    @Override
    protected Widget build() {
        Collection<? extends Column<? super T>> cols = table.columns;
        Collection<? extends T> rows = table.rows;

        return Align.top(grid(cols.size(), g -> {
            for (Column<? super T> col : cols) {
                // TODO horizontalAlignment?
                g.add(new Text(col.title()));
            }

            for (T row : rows) {
                for (Column<? super T> col : cols) {
                    Widget c = col.cellContentFunction().apply(row);
                    Objects.requireNonNull(c);
                    // TODO horizontalAlignment?
                    g.add(c);
                }
                if (table.clickHandler != null) {
                    g.cursorY--;
                    g.add(rowClickHandlerSlots.use(row, new TableClickHandler<>(table, row)), cols.size(), 1);
                }
            }
        }));
    }

    private static final class TableClickHandler<T> extends Widget {

        private final Table<T> table;
        private final T row;

        private TableClickHandler(Table<T> table, T row) {
            this.table = table;
            this.row = row;
        }

        @Override
        protected Widget build() {
            Widget content = new PointerStateDependent(
                    new ColorFill(Color.TRANSPARENT),
                    new ColorFill(Color.TRANSPARENT),
                    new ColorFill(Color.of("#fee5"))
            );
            return new ClickListener(content,
                    () -> table.clickHandler.accept(row));
        }
    }
}
