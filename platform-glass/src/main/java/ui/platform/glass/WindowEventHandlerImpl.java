package ui.platform.glass;

import com.sun.glass.events.WindowEvent;
import com.sun.glass.ui.Window;

class WindowEventHandlerImpl extends Window.EventHandler {
    @Override
    public void handleWindowEvent(Window window, long time, int type) {
        //System.out.println(WindowEvent.getEventName(type));
        if (type == WindowEvent.CLOSE) {
            window.close();
            System.exit(0);
        }
    }
}
