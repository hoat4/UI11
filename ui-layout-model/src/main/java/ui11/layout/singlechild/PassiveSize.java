package ui11.layout.singlechild;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class PassiveSize extends SubstitutedWidget {

    @Nonnull
    private final Widget content;

    public PassiveSize(@Nonnull Widget content) {
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public Widget content() {
        return content;
    }
}
