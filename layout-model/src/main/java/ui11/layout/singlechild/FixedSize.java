package ui11.layout.singlechild;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.decoration.Box;
import ui11.layout.LayoutSize;
import ui11.geom.Length;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

// vagy inkább PreferredSize?
public final class FixedSize extends SubstitutedWidget {

    private final LayoutSize size;
    private final Widget content;

    public FixedSize(LayoutSize size, Widget content) {
        Objects.requireNonNull(size);
        Objects.requireNonNull(content);
        this.size = size;
        this.content = content;
    }

    public FixedSize(Length width, Length height, Widget content) {
        this(new LayoutSize(width, height), content);
    }

    public FixedSize(Length widthAndHeight, Widget content) {
        this(new LayoutSize(widthAndHeight, widthAndHeight), content);
    }

    public static FixedSize withSize(Length prefWidthAndHeight, Widget content) {
        return new FixedSize(prefWidthAndHeight, content);
    }

    public static FixedSize withSize(Length prefWidth, Length prefHeight, Widget content) {
        return new FixedSize(prefWidth, prefHeight, content);
    }

    public static FixedSize withWidth(Length prefWidth, Widget content) {
        return withSize(prefWidth, null, content);
    }

    public static FixedSize withHeight(Length prefHeight, Widget content) {
        return withSize(null, prefHeight, content);
    }

    public LayoutSize size() {
        return size;
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return new Box(content).withFixedSize(size);
    }

    @Override
    public String toString() {
        return "FixedSize[" +
                "size=" + size + ", " +
                "content=" + content + ']';
    }
}
