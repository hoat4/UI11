package ui11.platform.awt;

import ui11.RootElement;
import ui11.Widget;
import ui11.window.Desktop;
import ui11.window.Shell;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class AWTDesktop implements Desktop, Shell {

    private static final AWTDesktop INSTANCE = new AWTDesktop();

    @Override
    public void openWindow(Widget content) {
        new RootElement(new AWTWindow(this, content).new Root(), EventQueue::invokeLater).start();
    }

    @Override
    public void openURL(URI target) {
        try {
            java.awt.Desktop.getDesktop().browse(target);
        } catch (IOException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    public static AWTDesktop instance() {
        return INSTANCE;
    }
}
