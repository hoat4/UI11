package ui11.platform.awt;

import ui11.platform.awt.AWTDesktop.AWTShellImpl;
import ui11.window.Desktop;
import ui11.window.Desktop.DesktopProvider;
import ui11.window.Shell;
import ui11.window.Shell.ShellProvider;

import java.awt.*;

public class AWTDesktopProvider implements DesktopProvider, ShellProvider {

    static final AWTShellImpl DEFAULT_SHELL = new AWTShellImpl(null);

    @Override
    public boolean isAvailable() {
        return !GraphicsEnvironment.isHeadless();
    }

    @Override
    public Desktop desktop() {
        return AWTDesktop.instance();
    }

    @Override
    public Shell shell() {
        return DEFAULT_SHELL;
    }
}
