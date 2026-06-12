package ui11.input.focus;

import ui11.Widget;
import ui11.color.Color;
import ui11.input.gesture.ClickListener;
import ui11.text.Text;
import ui11.window.Window;

import static ui11.decoration.Background.withBackground;
import static ui11.layout.multichild.LinearLayout.row;

public class FocusRootTest {

    public static void main() {
        FocusHolder h1 = new FocusHolder(), h2 = new FocusHolder();
        Window.open(row(
                withBackground(Color.YELLOW, new FocusableRect("h1", h1)),
                withBackground(Color.CYAN, new FocusableRect("h2", h2))
        ));
    }

    private static final class FocusableRect extends Widget {

        private final String name;
        private final FocusHolder h;

        @Inject private FocusRoot focusRoot;

        private FocusableRect(String name, FocusHolder h) {
            this.name = name;
            this.h = h;
        }

        @Override
        protected Widget build() {
            return new ClickListener(
                    new Text(name + " focused: " + focusRoot.isFocused(h)),
                    () -> focusRoot.requestFocus(h)
            );
        }
    }
}
