package ui11.imageio;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.media.JPEGImageView;

public class ImageViewResolverProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.add(JPEGImageView.class,
                jpg -> new ImageViewImpl(jpg.source()));
    }
}
