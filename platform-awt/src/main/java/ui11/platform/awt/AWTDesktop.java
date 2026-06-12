package ui11.platform.awt;

import ui11.WidgetTree;
import ui11.Widget;
import ui11.window.Desktop;
import ui11.window.Shell;

import org.jspecify.annotations.Nullable;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

public class AWTDesktop implements Desktop {

    private static final AWTDesktop INSTANCE = new AWTDesktop();

    @Override
    public void openWindow(Widget content) {
        try {
            // azért nem invokeLater, hogy ha nem sikerül a rootot buildelni vagy az ablakot megjeleníteni,
            // akkor értesüljön róla a hívó.
            // de valójában most nem teljesül talán egyik se, mert elkapjuk az exceptiont.
            EventQueue.invokeAndWait(() -> {
                WidgetTree.create(new AWTWindow(this, content).new Root(), EventQueue::invokeLater);
            });
        } catch (InterruptedException e) { // TODO ezzel mi legyen?
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException e2)
                throw new RuntimeException("Can't open window: " + e, e);
            else
                throw (Error) e.getCause();
        }
    }

    public static AWTDesktop instance() {
        return INSTANCE;
    }

    public static class AWTShellImpl implements Shell {

        // TODO ez így most hülyeség hogy itt is meg kell adni az URLResolvert, meg Provide-olni is kell

        private final URLResolver urlResolver;

        public AWTShellImpl(@Nullable URLResolver urlResolver) {
            this.urlResolver = urlResolver;
        }

        @Override
        public void openURL(URI target) {
            if (!target.isAbsolute()) {
                if (urlResolver == null)
                    throw new RuntimeException("no "+URLResolver.class.getSimpleName()+" " +
                            "but URL is relative: "+target);
                target = urlResolver.toAbsoluteURL(target);
            }
            if (!target.isAbsolute())
                throw new RuntimeException(URLResolver.class.getSimpleName() +
                        ".toAbsoluteURL returned relative URL: " + target);

            try {
                java.awt.Desktop.getDesktop().browse(target);
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO
            }
        }
    }
}
