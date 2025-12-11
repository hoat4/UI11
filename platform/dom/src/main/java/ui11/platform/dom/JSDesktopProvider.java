package ui11.platform.dom;

import ui11.Widget;
import ui11.window.Desktop;
import ui11.window.Desktop.DesktopProvider;
import org.teavm.interop.PlatformMarker;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLHeadElement;
import org.teavm.jso.dom.xml.Element;

public class JSDesktopProvider implements DesktopProvider {

    private final JSDesktop desktop = new JSDesktop();

    @Override
    public boolean isAvailable() {
        return isTeaVM();
    }

    @Override
    public Desktop desktop() {
        return desktop;
    }

    @PlatformMarker
    private static boolean isTeaVM() {
        return false;
    }

    private static class JSDesktop implements Desktop {

        @Override
        public void openWindow(Widget content) {
            Window w = Window.current().open("about:blank", "", "popup");

            // copy stylesheets
            HTMLHeadElement srcHead = Window.current().getDocument().getHead();
            HTMLHeadElement dstHead = w.getDocument().getHead();
            for (int i = 0; i < srcHead.getChildren().getLength(); i++) {
                Element e = srcHead.getChildren().item(i);
                if (e.getNodeName().equalsIgnoreCase("style") ||
                        e.getNodeName().equalsIgnoreCase("link") &&
                                "stylesheet".equalsIgnoreCase(e.getAttribute("rel")))
                    dstHead.appendChild(e.cloneNode(true));
            }

            new DOMEnvironment(w).doShow(content, w.getDocument().getBody());
        }
    }
}
