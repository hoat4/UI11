package ui11.input.focus;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.observable.MutableObservable;

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

    public @NonNull Widget content() {
        return content;
    }

    public @NonNull Runnable onFocused() {
        return onFocused;
    }

    public @NonNull Runnable onFocusLost() {
        return onFocusLost;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content;
    }
}
