package ui11.window;

import ui11.Widget;

import java.util.Iterator;
import java.util.ServiceLoader;

public interface Desktop {

    void openWindow(Widget content);

    // Desktop withScope(Scope scope);

    static Desktop getDesktop() {
        // TODO mi legyen ha több provider elérhető?
        for (Iterator<DesktopProvider> iterator = ServiceLoader.load(DesktopProvider.class).iterator();
             iterator.hasNext(); ) {
            DesktopProvider p = iterator.next();
            if (p.isAvailable())
                return p.desktop();
        }
        throw new UnsupportedOperationException("no DesktopProvider available");
    }

    interface DesktopProvider {

        boolean isAvailable();

        Desktop desktop();
    }
}
