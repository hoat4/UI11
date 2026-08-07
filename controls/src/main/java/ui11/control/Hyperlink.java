package ui11.control;

import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import java.net.URI;
import java.util.Objects;

public final class Hyperlink extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @NonNull URI target;

    @Remember private Key contentKey;

    public Hyperlink(@NonNull Widget content, @NonNull URI target) {
        this.content = Objects.requireNonNull(content);
        this.target = Objects.requireNonNull(target);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected Hyperlink forSubstitution() {
        return new Hyperlink(
                content.withKey(contentKey),
                target
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull URI target() {
        return target;
    }
}
