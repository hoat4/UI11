package ui11.control;

import ui11.SubstitutedWidget;
import ui11.input.focus.FocusHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class PlainTextEditor extends SubstitutedWidget {

    private final EditablePlainText editablePlainText;
    @Nullable @Listener private final Runnable onAction;
    @Nonnull private final FocusHolder focusHolder;

    public PlainTextEditor(EditablePlainText editablePlainText,
                           @Nullable Runnable onAction,
                           @Nonnull FocusHolder focusHolder) {
        this.editablePlainText = Objects.requireNonNull(editablePlainText);
        this.onAction = onAction;
        this.focusHolder = Objects.requireNonNull(focusHolder);
    }

    public EditablePlainText editablePlainText() {
        return editablePlainText;
    }

    @Nullable
    public Runnable onAction() {
        return onAction;
    }

    public FocusHolder focusHolder() {
        return focusHolder;
    }
}