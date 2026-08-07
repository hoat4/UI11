package ui11.decoration;

import org.jspecify.annotations.NonNull;
import ui11.Key;
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

    @Remember private Key backgroundKey;
    @Remember private Key contentKey;

    public Background(@Nullable Widget background, @NonNull Widget content) {
        this.background = Gone.goneIfNull(background);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
        backgroundKey = Key.create();
    }

    @Override
    protected Background forSubstitution() {
        return new Background(
                background.withKey(backgroundKey),
                content.withKey(contentKey)
        );
    }

    public @NonNull Widget background() {
        return background;
    }

    public @NonNull Widget content() {
        return content;
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
