package ui11.platform.awt.j2d;

import ui11.graphics.fill.RasterImage;
import ui11.graphics.fill.ImageLoader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AWTImageLoader implements ImageLoader {

    private static final Map<URL, RasterImage> cache = new ConcurrentHashMap<>();

    @Override
    public RasterImage loadImage(URL url) throws IOException {
        return doLoadImage(url);
    }

    // TODO @DontInline
    private static RasterImage doLoadImage(URL url) throws IOException {
        if (cache.containsKey(url))
            return cache.get(url);
        J2DImageImpl img = J2DImageImpl.of(ImageIO.read(url));
        cache.putIfAbsent(url, img);
        return img;
    }
}
