package ui11.imageio;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.ResolverRegistry.Priority;
import ui11.graphics.fill.RasterImageView;
import ui11.media.JPEGImageView;

public class ImageViewResolverProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.add(Priority.EMULATED, JPEGImageView.class, jpg -> new ImageViewImpl(jpg.source())).
                offers(RasterImageView.class);
    }
}
