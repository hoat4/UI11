package ui11.control;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;

public final class Hyperlink extends SubstitutedWidget {

    @Nonnull private final Widget content;
    @Nonnull private final URI target;

    public Hyperlink(@Nonnull Widget content, @Nonnull URI target) {
        this.content = Objects.requireNonNull(content);
        this.target = Objects.requireNonNull(target);
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    @Nonnull
    public URI target() {
        return target;
    }
}
