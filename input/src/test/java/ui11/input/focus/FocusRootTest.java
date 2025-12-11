package ui11.input.focus;

import ui11.Widget;
import ui11.graphics.fill.Color;
import ui11.input.gesture.ClickListener;
import ui11.observable.Observable;
import ui11.text.Text;
import ui11.window.Desktop;

import static ui11.decoration.Background.withBackground;
import static ui11.layout.multichild.LinearLayout.row;

public class FocusRootTest {

    public static void main() {
        FocusHolder h1 = new FocusHolder(), h2 = new FocusHolder();
        Desktop.getDesktop().openWindow(
                row(
                        withBackground(Color.YELLOW, new FocusableRect("h1", h1)),
                        withBackground(Color.CYAN, new FocusableRect("h2", h2))
                )
        );
    }

    private static final class FocusableRect extends Widget {
        private final String name;
        private final FocusHolder h;

        @Inject private Observable<FocusRoot> focusRoot;

        private FocusableRect(String name, FocusHolder h) {
            this.name = name;
            this.h = h;
        }

        @Override
        protected Widget build() {
            return new ClickListener(
                    new Text(name + " focused: " + focusRoot.get().isFocused(h)),
                    () -> this.focusRoot.get().requestFocus(h)
            );
        }
    }
}
