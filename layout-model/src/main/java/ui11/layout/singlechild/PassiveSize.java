package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class PassiveSize extends SubstitutedWidget {

    private final @NonNull Widget content;

    @Remember private Key contentKey;

    public PassiveSize(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected PassiveSize forSubstitution() {
        return new PassiveSize(
                content.withKey(contentKey)
        );
    }

    public @NonNull Widget content() {
        return content;
    }
}
