package ui11.control;

import ui11.window.Window;

public class SingleTextFieldTest {
    public static void main(String[] args) {
        // volt egy olyan bug (2025-11-29), hogy 3-szor hozott létre pl. AWTEnterContentListenerPeert,
        // mert MultiChildLayoutban el voltak rontva a keyek
        Window.open(new TextField(new EditablePlainText()));
    }
}
