package ui11.window;

import ui11.window.Shell.ShellProvider;

import java.net.URI;
import java.util.ServiceLoader;

// TODO értelmesebb név
public interface Shell {

    void openURL(URI target);

    static Shell getShell() {
        return ServiceLoader.load(Shell.ShellProvider.class).findFirst().get().shell();
    }

    interface ShellProvider {

        // TODO boolean isAvailable();

        Shell shell();
    }
}
