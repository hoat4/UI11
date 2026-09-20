package ui11.renderer;

import ui11.graphics.VisualContentRequest;

import java.util.ServiceLoader;

public interface Renderer<N extends Node, D> {

    VisualContentRequest<N> createRootContentRequest();

    D prepare(N root);

    /**
     * If there is a separate paint thread, this will be called in that thread only once, before all renders.
     * If there are no paint threads, this will be called before every frame.
     */
    void initializeRenderThreadLocals();

    /**
     * if there is a separate paint thread, this will be called in that thread
     */
    void render(D displayList);

    static Renderer<?, ?> create(RenderableSurface surface) {
        Renderer<?, ?> r = null;
        for (RendererProvider provider : ServiceLoader.load(RendererProvider.class)) {
            Renderer<?, ?> r2 = provider.tryProvide(surface);
            if (r2 != null)
                if (r != null)
                    throw new RuntimeException("Multiple renderer available for " + surface +
                            ", at least: " + r + " and " + r2);
                else
                    r = r2;
        }
        if (r == null)
            throw new RuntimeException("No renderer available for " + surface);
        return r;
    }
}
