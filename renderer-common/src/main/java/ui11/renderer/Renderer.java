package ui11.renderer;

import org.jspecify.annotations.Nullable;
import ui11.renderer.layer.Layer;

import java.util.ServiceLoader;

public interface Renderer<L extends Layer> {

    L createLayer(@Nullable Layer previous);

    /**
     * If there is a separate paint thread, this will be called in that thread only once, before all renders.
     * If there are no paint threads, this will be called before every frame.
     */
    void initializeRenderThreadLocals();

    /**
     * if there is a separate paint thread, this will be called in that thread
     */
    void render(L rootLayer, int viewportWidth, int viewportHeight);
    // TODO ezt a viewportWidth és viewportHeight paramétereket szedjük ki innen

    static Renderer<?> create(RenderableSurface surface) {
        Renderer<?> r = null;
        for (RendererProvider provider : ServiceLoader.load(RendererProvider.class)) {
            Renderer<?> r2 = provider.tryProvide(surface);
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
