package ui11.renderer;

import org.jspecify.annotations.Nullable;

public interface RendererProvider {

    @Nullable
    Renderer<?, ?> tryProvide(Surface surface);
}
