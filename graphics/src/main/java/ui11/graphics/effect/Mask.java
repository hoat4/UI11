package ui11.graphics.effect;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// TODO mask módok (luminosity, alpha, stb.)

public final class Mask extends SubstitutedWidget {

    private final Widget content;
    private final Widget mask;

    @Remember private Key contentKey;
    @Remember private Key maskKey;

    public Mask(@NonNull Widget content, @NonNull Widget mask) {
        this.content = Objects.requireNonNull(content);
        this.mask = Objects.requireNonNull(mask);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
        maskKey = Key.create();
    }

    @Override
    protected Mask forSubstitution() {
        return new Mask(
                content.withKey(contentKey),
                mask.withKey(maskKey)
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Widget mask() {
        return mask;
    }
}
