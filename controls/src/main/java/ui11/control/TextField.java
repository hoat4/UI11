package ui11.control;

import ui11.resolution.SubstitutedWidget;

import org.jspecify.annotations.Nullable;
import java.util.Objects;

public final class TextField extends SubstitutedWidget {

    private final EditablePlainText text;
    @Nullable private final Runnable onAction;
    private final boolean autofocus;

    public TextField(EditablePlainText editablePlainText) {
        this(editablePlainText, null, false);
    }

    public TextField(EditablePlainText editablePlainText, @Nullable Runnable onAction) {
        this(editablePlainText, onAction, false);
    }

    public TextField(EditablePlainText text,
                     @Nullable Runnable onAction,
                     boolean autofocus) {
        this.text = text;
        this.onAction = listenerProxy(onAction);
        this.autofocus = autofocus;
        Objects.requireNonNull(text);
    }

    public EditablePlainText text() {
        return text;
    }

    @Nullable
    public Runnable onAction() {
        return onAction;
    }

    public boolean isAutofocus() {
        return autofocus;
    }

    // egy időben a fallback content egy PlainTextEditor volt, de valszeg nincs értelme
}
