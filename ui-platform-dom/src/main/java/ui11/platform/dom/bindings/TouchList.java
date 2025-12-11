package ui11.platform.dom.bindings;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.JSMethod;

public abstract class TouchList implements JSObject {
    @JSProperty
    public abstract int getLength();

    @JSMethod
    public abstract JSTouch item(int index);
}
