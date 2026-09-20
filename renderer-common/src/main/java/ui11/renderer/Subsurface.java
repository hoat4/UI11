package ui11.renderer;

import org.jspecify.annotations.NonNull;
import ui11.graphics.Surface;
import ui11.observable.MutableObservable;

public abstract class Subsurface implements Surface {

    public final MutableObservable<Surface> parent = MutableObservable.ofNullable();

    public @NonNull Surface parent() {
        Surface p = parent.get();
        if (p == null)
            throw new IllegalStateException("No parent");
        return p;
    }
}
