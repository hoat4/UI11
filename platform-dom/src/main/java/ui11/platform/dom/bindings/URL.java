package ui11.platform.dom.bindings;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;


public class URL implements JSObject {
    @JSBody(params = {"blob"}, script = "var urlCreator = window.URL || window.webkitURL; return urlCreator.createObjectURL(blob);")
    private static native String blobToURL(Blob blob);

    @JSBody(params = "url", script = "var urlCreator = window.URL || window.webkitURL; return urlCreator.revokeObjectURL(url);")
    public static native void revokeURL(String url);
    
    public static String toURL(Blob b) {
        return blobToURL(b);
    }
}

