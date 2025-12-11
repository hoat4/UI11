package ui11.input.focus;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;
import java.util.Objects;

// TODO
public final class FocusHolderWrapper extends SubstitutedWidget {

    @Nonnull private final FocusHolder holder;
    @Nonnull private final Widget content;

    public FocusHolderWrapper(@Nonnull FocusHolder holder, @Nonnull Widget content) {
        this.holder = Objects.requireNonNull(holder);
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public FocusHolder holder() {
        return holder;
    }

    @Nonnull
    public Widget content() {
        return content;
    }
}
