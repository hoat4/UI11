package ui11.control;

import org.jspecify.annotations.NonNull;
import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.text.Text;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 *
 */
public final class Button extends SubstitutedWidget {

    private final Widget content;
    @Nullable private final Runnable actionHandler;

    @Remember private Key contentKey;

    /**
     * @param actionHandler ha ez null, akkor disablednek számít a gomb
     */
    public Button(@NonNull Widget content, @Nullable Runnable actionHandler) {
        this.content = Objects.requireNonNull(content);
        this.actionHandler = listenerProxy(actionHandler);
    }

    /**
     * @param actionHandler ha ez null, akkor disablednek számít a gomb
     */
    public Button(String caption, @Nullable Runnable actionHandler) {
        this(new Text(caption), actionHandler);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected Button forSubstitution() {
        return new Button(
                content.withKey(contentKey),
                actionHandler
        );
    }

    public boolean enabled() {
        return actionHandler != null;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Nullable
    public Runnable actionHandler() {
        return actionHandler;
    }

    @Override
    public String toString() {
        return "Button[" +
                "content=" + content + ", " +
                "actionHandler=" + actionHandler + ']';
    }

    public static final class ButtonState extends SubstitutedWidget {

        private final Button button;
        private final boolean pressed;

        public ButtonState(Button button, boolean pressed) {
            this.button = button;
            this.pressed = pressed;
        }

        public Button button() {
            return button;
        }

        public boolean pressed() {
            return pressed;
        }

        @Override
        public String toString() {
            return "ButtonState[" +
                    "button=" + button + ", " +
                    "pressed=" + pressed + ']';
        }
    }
}
