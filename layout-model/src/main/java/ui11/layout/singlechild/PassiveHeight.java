package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

/**
 * Hagyja, hogy a szülő beállítson bármilyen magasságot, viszont a preferált szélességet úgy határozza meg, hogy
 * megfeleljen az elem preferált aspect ratiojának és a szülő által meghatározott magasságnak is.
 */
public final class PassiveHeight extends SubstitutedWidget {

    private final Widget content;
    private final double aspectRatio;

    @Remember private Key contentKey;

    public PassiveHeight(@NonNull Widget content, double aspectRatio) {
        if (aspectRatio < 0 && aspectRatio != -1 || !Double.isFinite(aspectRatio))
            throw new IllegalArgumentException();
        this.content = Objects.requireNonNull(content);
        this.aspectRatio = aspectRatio;
    }

    public PassiveHeight(Widget content) {
        this(content, -1);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected PassiveHeight forSubstitution() {
        return new PassiveHeight(
                content.withKey(contentKey),
                aspectRatio
        );
    }

    public Widget content() {
        return content;
    }

    public double aspectRatio() {
        return aspectRatio;
    }
}
