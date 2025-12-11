package ui11.platform.awt;

import ui11.observable.MutableObservable;
import ui11.geom.Location;
import ui11.input.pointer.Pointer.MouseCursor;

import java.util.Set;

public class AWTMouse implements MouseCursor {

    public static final AWTMouse INSTANCE = new AWTMouse();

    final MutableObservable<Location> location = MutableObservable.ofNullable();

    private AWTMouse() {
    }

    @Override
    public Set<? extends Button> pressedButtons() {
        throw new RuntimeException("TODO");
    }

    @Override
    public Location location() {
        if (location.get() == null)
            throw new RuntimeException("TODO");
        return location.get();
    }
}
