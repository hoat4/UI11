package ui11.input.pointer;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class WithCursor extends SubstitutedWidget {

    private final @NonNull Cursor cursor;
    private final @NonNull Widget content;

    @Remember private Slot2 contentSlot;

    public WithCursor(Cursor cursor, @NonNull Widget content) {
        this.cursor = Objects.requireNonNull(cursor);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected WithCursor forSubstitution() {
        return new WithCursor(cursor, contentSlot.with(content));
    }

    public @NonNull Cursor cursor() {
        return cursor;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }

    public interface Cursor {
    }

    public enum StandardCursor implements Cursor {

        ARROW,

        /**
         * mint CSS-ben {@code cursor: pointer;}
         */
        HAND,

        TEXT
    }
}
