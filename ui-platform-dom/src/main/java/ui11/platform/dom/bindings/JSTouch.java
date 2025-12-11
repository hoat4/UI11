package ui11.platform.dom.bindings;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

public abstract class JSTouch implements JSObject {
    @JSProperty
    public abstract int getIdentifier();

    @JSProperty
    public abstract int getScreenX();

    @JSProperty
    public abstract int getScreenY();

    @JSProperty
    public abstract int getClientX();

    @JSProperty
    public abstract int getClientY();

    @JSProperty
    public abstract int getPageX();

    @JSProperty
    public abstract int getPageY();
}
