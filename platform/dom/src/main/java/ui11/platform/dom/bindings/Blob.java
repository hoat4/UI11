package ui11.platform.dom.bindings;

import org.teavm.jso.JSObject;
import org.teavm.jso.JSBody;
import org.teavm.jso.JSProperty;

public abstract class Blob implements JSObject {

    @JSBody(params = "object", script = "return new Blob([object]);")
    public static native Blob create(JSObject object);

    @JSBody(params = {"object", "_type"}, script = "return new Blob([object], {type: _type});")
    public static native Blob create(JSObject object, String _type);

    @JSBody(params = {"object", "_type"}, script = "return new Blob([object], {type: _type});")
    public static native Blob create(String object, String _type);
    
    @JSProperty
    public abstract int getSize();

    @JSProperty
    public abstract String getType();

    @JSBody(params = {"b", "start", "end"}, script = "return b.slice(start, end);")
    public static native Blob slice(Blob b, int start, int end);

    @JSBody(params = {"b", "start", "end", "newType"}, script = "return b.slice(start, end, newType);")
    public static native Blob slice(Blob b, int start, int end, String newType);
}
