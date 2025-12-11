package ui11.input.pointer;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class WithCursor extends SubstitutedWidget {

    @Nonnull private final Cursor cursor;
    @Nonnull private final Widget content;

    public WithCursor(Cursor cursor, Widget content) {
        this.cursor = Objects.requireNonNull(cursor);
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public Cursor cursor() {
        return cursor;
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
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
