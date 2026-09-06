package ui11.renderer.j2d;

import org.jspecify.annotations.Nullable;
import ui11.platform.awt.AWTFrameSurface;
import ui11.renderer.Renderer;
import ui11.renderer.RendererProvider;
import ui11.renderer.Surface;

public class J2DRendererProvider implements RendererProvider {
    @Override
    public @Nullable Renderer<?, ?> tryProvide(Surface surface) {
        if (surface instanceof AWTFrameSurface awtFrameSurface)
            return new J2DRenderer(awtFrameSurface);
        else
            return null;
    }
}
