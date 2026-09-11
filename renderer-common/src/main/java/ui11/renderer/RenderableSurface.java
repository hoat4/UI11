package ui11.renderer;

import ui11.geom.Size;
import ui11.graphics.Surface;

/**
 * A {@linkplain Surface} that is backed by a framebuffer, either in RAM or in GPU.
 */
public interface RenderableSurface extends Surface {

    int width();

    int height();

    @Override
    default Size size() {
        return new Size(width(), height());
    }
}
