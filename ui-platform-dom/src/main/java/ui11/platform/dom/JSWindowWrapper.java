package ui11.platform.dom;

import org.teavm.interop.PlatformMarker;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.browser.TimerHandler;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.html.HTMLElement;

public interface JSWindowWrapper extends JSObject {

    @JSBody(params = {"handler", "delay"}, script = "return setTimeout(handler, delay);")
    int setTimeout(TimerHandler handler, double delay);

    @JSBody(params = {"id"}, script = "clearTimeout(id);")
    void clearTimeout(int id);

    @JSBody(params = {"handler", "delay"}, script = "return setInterval(handler, delay);")
    int setInterval(TimerHandler handler, double delay);

    @JSBody(params = {"id"}, script = "clearInterval(id);")
    void clearInterval(int id);

    @JSBody(script = "return window;")
    Window getWindow();

    @JSBody(script = "nativeObj.dpeer = javaObj;", params = {"nativeObj", "javaObj"})
    void setData(JSObject nativeObj, Object javaObj);

    @JSBody(script = "return nativeObj.dpeer || null;", params = {"nativeObj"})
    Object getData(JSObject nativeObj);

    @JSBody(script = "return evt.buttons !== undefined;", params = {"evt"})
    boolean hasButtonsProperty(MouseEvent mouseEvent);

    @JSBody(script = "eventTarget.addEventListener(eventName, eventHandler, {passive: false});",
            params = {"eventTarget", "eventName", "eventHandler"})
    void addNonPassiveEventListener(EventTarget eventTarget,
                                    String eventName,
                                    EventListener<?> eventHandler);

    // TeaVM beépített nem double-t, hanem intet fogad
    @JSBody(script = "return document.elementFromPoint(x, y);",
            params = {"x", "y"})
    HTMLElement elementFromPoint(double x, double y);

    static JSWindowWrapper ofNative() {
        return ofNative(Window.current());
    }

    static JSWindowWrapper ofNative(org.teavm.jso.browser.Window w) {
        return (JSWindowWrapper) w;
    }

    @PlatformMarker
    static boolean isNativeAvailable() {
        return false;
    }

    // TeaVM beépített nem double-t, hanem intet fogad
    @JSBody(script = "e.scrollLeft = x; e.scrollTop = y;", params = {"e", "x", "y"})
    void setScrollPos(HTMLElement htmlElement, double x, double y);

    @JSBody(script = "return e.scrollLeft;", params = {"e"})
    double getScrollLeft(HTMLElement htmlElement);

    @JSBody(script = "return e.scrollTop;", params = {"e"})
    double getScrollTop(HTMLElement htmlElement);

    @JSBody(script = "return e.scrollWidth;", params = {"e"})
    double getScrollWidth(HTMLElement htmlElement);

    @JSBody(script = "return e.scrollHeight;", params = {"e"})
    double getScrollHeight(HTMLElement htmlElement);

    @JSBody(script = "return e.offsetWidth;", params = {"e"})
    double getOffsetWidth(HTMLElement htmlElement);

    @JSBody(script = "return e.offsetHeight;", params = {"e"})
    double getOffsetHeight(HTMLElement htmlElement);
}
