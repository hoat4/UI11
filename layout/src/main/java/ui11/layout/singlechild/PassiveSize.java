package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

public final class PassiveSize extends SubstitutedWidget {

    private final @NonNull Widget content;

    public PassiveSize(@NonNull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected PassiveSize forSubstitution() {
        return new PassiveSize(
                withID("content", content)
        );
    }

    public @NonNull Widget content() {
        return content;
    }
}
