package ui11.platform.dom.peers;

import org.teavm.jso.JSClass;
import ui11.Widget;
import ui11.input.gesture.CloseRequestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.EventTarget;

public class DOMCloseRequestListenerPeer extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(DOMCloseRequestListenerPeer.class);

    private static int closeWatcherDebugCounter;

    private final CloseRequestListener widget;

    @Remember private CloseWatcher closeWatcher;
    @Remember private String debugPrefix;

    public DOMCloseRequestListenerPeer(CloseRequestListener widget) {
        this.widget = widget;
    }

    @Override
    protected void onResume() {
        if (!isSupported()) {
            logger.debug("CloseWatcher not supported");
            // ilyenkor ezt az Elementet se kéne létrehozni
            return;
        }

        // TODO closewatcherek sorrendje
        // TODO ha nincs descendant fókuszálva, lehet hogy meg kéne szüntetni a closewatchert

        debugPrefix = "[CloseWatcher #" + (++closeWatcherDebugCounter) + "]";
        logger.debug(debugPrefix + " Created");
        closeWatcher = CloseWatcher.create();
        untilPause().onClose(() -> {
            logger.debug(debugPrefix + " Destroy");
            closeWatcher.destroy();
            closeWatcher = null;
        });
    }

    @Override
    protected Widget build() {
        if (isSupported()) {
            untilNextRebuild().onClose(closeWatcher.onEvent("close", evt -> {
                logger.debug(debugPrefix + " Closed");
                widget.onClose().run();
            })::dispose);
        }

        return widget.content();
    }

    @JSBody(script = "return !!window.CloseWatcher;")
    private static native boolean isSupported();

    // transparent csak azért, mert nem minden böngészőben van (#5247),
    // és fieldbe íráskor kasztolna rá
    @JSClass(transparent = true)
    private static abstract class CloseWatcher implements JSObject, EventTarget {

        public abstract void close();

        public abstract void destroy();

        public abstract void requestClose();

        @JSBody(script = "return new CloseWatcher();")
        public static native CloseWatcher create();
    }
}
