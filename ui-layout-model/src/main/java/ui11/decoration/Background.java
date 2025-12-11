package ui11.decoration;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Gone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO nullság ellenőrzése
public final class Background extends SubstitutedWidget {

    private final Widget background;
    private final Widget content;

    public Background(Widget background, Widget content) {
        this.background = background;
        this.content = content;
    }

    public Widget background() {
        return background;
    }

    public Widget content() {
        return content;
    }

    public static Widget withBackground(@Nullable Widget bg, Widget e) {
        // nem adunk vissza e-t ha bg null, mert úgy megváltoznának az implicit keyek
        return new Background(Gone.goneIfNull(bg), e);
    }

    public static Widget withBackground(@Nullable Color bg, Widget e) {
        return new Background(bg == null ? null : new ColorFill(bg), e);
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new Box(content).withBackground(background);
    }
}
