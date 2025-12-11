package ui11.input.focus;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class FocusListener extends SubstitutedWidget {
    private final Widget content;
    private final Runnable onFocused;
    private final Runnable onFocusLost;

    public FocusListener(Widget content, Runnable onFocused, Runnable onFocusLost) {
        this.content = Objects.requireNonNull(content);
        this.onFocused = Objects.requireNonNull(onFocused);
        this.onFocusLost = Objects.requireNonNull(onFocusLost);
    }

    public FocusListener(Widget content, MutableObservable<Boolean> target) {
        this(content, () -> target.set(true), () -> target.set(false));
        // TODO ez nem jó, mert ha új képződik, akkor nem állítja vissza false-ra
    }

    public Widget content() {
        return content;
    }

    public Runnable onFocused() {
        return onFocused;
    }

    public Runnable onFocusLost() {
        return onFocusLost;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return content;
    }
}
