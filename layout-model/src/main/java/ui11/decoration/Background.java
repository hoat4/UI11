package ui11.decoration;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class Background extends SubstitutedWidget {

    private final @NonNull Widget background;
    private final @NonNull Widget content;

    @Inject private Slot backgroundSlot;
    @Inject private Slot contentSlot;

    public Background(@Nullable Widget background, @NonNull Widget content) {
        this.background = Gone.goneIfNull(background);
        this.content = Objects.requireNonNull(content);
    }

    public @NonNull Widget background() {
        return backgroundSlot == null ? background : background.withSlot(backgroundSlot);
    }

    public @NonNull Widget content() {
        return contentSlot == null ? content : content.withSlot(contentSlot);
    }

    public static Widget withBackground(@Nullable Widget bg, @NonNull Widget e) {
        // nem adunk vissza e-t ha bg null, mert úgy megváltoznának az implicit keyek
        return new Background(bg, e);
    }

    public static Widget withBackground(@Nullable Color bg, @NonNull Widget e) {
        return new Background(bg == null ? null : new ColorFill(bg), e);
    }

    @Override
    protected Widget fallbackContent() {
        return new Box(content()).withBackground(background());
    }
}
