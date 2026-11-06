package ui11.media;

import ui11.Widget;

import java.util.Map;

// the resolution shouldn't be a property of Video because different variants can have different widget types:
// for example, on small resolution it's maybe enough to display a simple vectorized animation built from a Widget tree,
// but on a larger resolution, a raster video is needed

public final class ResolutionDependent extends Widget {

    private final Map<MediaResolution, Widget> variants;

    // TODO lehessen olyan megadni, hogy ismeretlen resolutionű default (pl. vektoros grafika)?

    public ResolutionDependent(Map<MediaResolution, Widget> variants) {
        this.variants = variants;
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("TODO");
    }

    public record MediaResolution(int width, int height) {}
}
