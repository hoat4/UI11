package ui11.platform.dom.bindings;

import org.teavm.jso.dom.events.*;

import ui11.geom.Vec2;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSProperty;


public abstract class PointerEvent implements MouseEvent {
    public static final String DOWN   = "pointerdown";
    public static final String UP     = "pointerup";
    public static final String MOVE   = "pointermove";
    public static final String CANCEL = "pointercancel";
    public static final String ENTER  = "pointerenter";
    public static final String LEAVE  = "pointerleave";

    public static final String TYPE_MOUSE = "mouse";
    public static final String TYPE_PEN = "pen";
    public static final String TYPE_TOUCH = "touch";

    @JSProperty
    public native int getWidth();
    
    @JSProperty
    public native int getHeight();

    @JSProperty
    public native int getPointerId();
    
    @JSProperty
    public native String getPointerType();
    
    @JSProperty
    public native boolean getIsPrimary();

    @JSBody(script = "return this.getCoalescedEvents()")
    public native PointerEvent[] getCoalescesdEvents();


    @JSBody(
        params = {"type", "cx", "cy"},
        script = """
            return new PointerEvent(type, {
                pointerId: -1,
                clientX: cx,
                clientY: cy,
                isPrimary: true,
            });
        """
    )
    public static native PointerEvent create(String type, double clientX, double clientY);
    
    /**
     * pointerId mindig -1
     */
    @JSBody(
        params = {"type"},
        script = """
            return new PointerEvent(type, {
                pointerId: -1,
                isPrimary: true,
            });
        """
    )
    public static native PointerEvent create(String type);


    @JSBody(script = """
        let pe = new PointerEvent(\"mouse\");
        return (\"getCoalescedEvents\" in pe);
    """)
    public static native boolean supportsPointerEventCoalescedEvents();
}
