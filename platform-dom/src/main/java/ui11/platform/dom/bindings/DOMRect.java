package ui11.platform.dom.bindings;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;

// TeaVM-ben lévő DOMRect nem jó, mert abban intet ad vissza getWidth és getHeight
public abstract class DOMRect implements JSObject {

    @JSProperty
    public abstract double getX();

    @JSProperty
    public abstract double getY();

    @JSProperty
    public abstract double getWidth();

    @JSProperty
    public abstract double getHeight();
}
