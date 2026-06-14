package ui.platform.glass;

import ui11.window.Desktop;

public class GlassDesktopProvider implements Desktop.DesktopProvider {

    private GlassDesktop desktop;

    @Override
    public boolean isAvailable() {
        return true; // TODO
    }

    @Override
    public synchronized Desktop desktop() {
        if (desktop == null)
            desktop = GlassDesktop.initialize();
        return desktop;
    }
}
