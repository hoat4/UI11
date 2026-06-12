package ui11.observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class SimpleScope implements Scope {

    private static final Logger logger = LoggerFactory.getLogger(SimpleScope.class);

    private List<Runnable> onClose = new ArrayList<>();

    public SimpleScope(Scope parent) {
        // TODO szedjük ki a listenert ha minket bezárnak
        parent.onClose(() -> {
            if (!isClosed())
                close();
        });
    }

    public boolean isClosed() {
        return onClose == null;
    }

    @Override
    public void onClose(Runnable closeListener) {
        if (onClose == null)
            throw new ScopeAlreadyClosedException();
        else
            this.onClose.add(closeListener);
    }

    public void close() {
        List<Runnable> l = onClose;
        if (l == null)
            throw new IllegalStateException("scope already closed");
        onClose = null;
        l.forEach(r -> {
            try {
                r.run();
            } catch (RuntimeException | AssertionError e) {
                logger.error("Scope close handler " + r + " failed with an exception", e);
            }
        });
    }
}
