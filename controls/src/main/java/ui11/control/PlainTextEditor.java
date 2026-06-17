package ui11.control;

import ui11.SubstitutedWidget;
import ui11.input.focus.FocusHolder;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.Objects;

public final class PlainTextEditor extends SubstitutedWidget {

    private final @NonNull EditablePlainText editablePlainText;
    private final @Nullable Runnable onAction;
    private final @Nullable FocusHolder focusHolder;

    public PlainTextEditor(@NonNull EditablePlainText editablePlainText) {
        this(editablePlainText, null, null);
    }

    public PlainTextEditor(@NonNull EditablePlainText editablePlainText,
                           @Nullable Runnable onAction,
                           @Nullable FocusHolder focusHolder) {
        this.editablePlainText = Objects.requireNonNull(editablePlainText);
        this.onAction = listenerProxy(onAction);
        this.focusHolder = focusHolder;
    }

    public @NonNull EditablePlainText editablePlainText() {
        return editablePlainText;
    }

    public @Nullable Runnable onAction() {
        return onAction;
    }

    public @Nullable FocusHolder focusHolder() {
        return focusHolder;
    }
}