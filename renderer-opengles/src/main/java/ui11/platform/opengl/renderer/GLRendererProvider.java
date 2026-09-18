package ui11.platform.opengl.renderer;

import org.jspecify.annotations.Nullable;
import ui11.renderer.NativeWindowSurface;
import ui11.renderer.RenderableSurface;
import ui11.renderer.Renderer;
import ui11.renderer.RendererProvider;

public class GLRendererProvider implements RendererProvider {
    @Override
    public @Nullable Renderer<?> tryProvide(RenderableSurface surface) {
        if (surface instanceof NativeWindowSurface nativeWindowSurface)
            return new GLRenderer(nativeWindowSurface.nativeWindowHandle().address());
        else
            return null;
    }
}
