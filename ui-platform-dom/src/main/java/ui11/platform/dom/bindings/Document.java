package ui11.platform.dom.bindings;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

public abstract class Document implements JSObject {
    @JSBody(script = "return document.visibilityState;")
    public static native String getVisibilityState();
}
