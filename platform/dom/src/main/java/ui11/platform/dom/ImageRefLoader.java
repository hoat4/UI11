package ui11.platform.dom;

import ui11.graphics.fill.RasterImage;
import ui11.graphics.fill.ImageLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.URL;

public class ImageRefLoader implements ImageLoader {

    @Override
    public RasterImage loadImage(URL url) throws IOException {
        return new URLImage(url, null);
    }

    public static class URLImage extends RasterImage {

        public final URL url;
        @Nullable public final SubimageCoords subimageCoords;

        public URLImage(URL url, @Nullable SubimageCoords subimageCoords) {
            super(1, 1); // TODO
            this.url = url;
            this.subimageCoords = subimageCoords;
        }

        @Override
        protected void readDataImpl(int x, int y, int w, int h, MemorySegment dst) {
            throw new RuntimeException();
        }

        @Override
        public RasterImage subimage(int x, int y, int w, int h) {
            return new URLImage(url, new SubimageCoords(x, y, w, h));
        }

        public record SubimageCoords(int x, int y, int w, int h) {}
    }
}
