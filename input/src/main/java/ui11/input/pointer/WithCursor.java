package ui11.input.pointer;

import org.jspecify.annotations.NonNull;
import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class WithCursor extends SubstitutedWidget {

    private final @NonNull Cursor cursor;
    private final @NonNull Widget content;

    public WithCursor(Cursor cursor, Widget content) {
        this.cursor = Objects.requireNonNull(cursor);
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Cursor cursor() {
        return cursor;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
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
