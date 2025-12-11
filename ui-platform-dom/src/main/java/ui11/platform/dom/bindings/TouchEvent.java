package ui11.platform.dom.bindings;

import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;

public interface TouchEvent extends Event {
    @JSProperty
    TouchList getTouches();

    @JSProperty
    TouchList getChangedTouches();

    @JSProperty
    TouchList getTargetTouches();
}
