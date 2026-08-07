package ui11.input.focus;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Objects;

// TODO
public final class FocusHolderWrapper extends SubstitutedWidget {

    private final @NonNull FocusHolder holder;
    private final @NonNull Widget content;

    @Remember private Key contentKey;

    public FocusHolderWrapper(@NonNull FocusHolder holder, @NonNull Widget content) {
        this.holder = Objects.requireNonNull(holder);
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected FocusHolderWrapper forSubstitution() {
        return new FocusHolderWrapper(holder, content.withKey(contentKey));
    }

    public @NonNull FocusHolder holder() {
        return holder;
    }

    public @NonNull Widget content() {
        return content;
    }
}
