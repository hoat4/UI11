package ui11.graphics;

import org.jspecify.annotations.NonNull;
import ui11.ExposeRequest;

import java.util.Objects;

@ExposeRequest.AtMostOnce
public abstract class VisualContentRequest<P> extends ExposeRequest<P> {

    public final Surface surface;

    protected VisualContentRequest(@NonNull Class<P> peerType, @NonNull Surface surface) {
        super(peerType);
        this.surface = Objects.requireNonNull(surface);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        VisualContentRequest<?> that = (VisualContentRequest<?>) o;
        return surface.equals(that.surface);
    }

    @Override
    public int hashCode() {
        return surface.hashCode();
    }
}
