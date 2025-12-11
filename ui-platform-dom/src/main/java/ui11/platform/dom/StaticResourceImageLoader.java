/*
package ui11.renderer.dom;

import ui11.graphics.Image;
import ui11.graphics.fill.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.MemorySegment;
import java.net.URL;
import java.util.Base64;

public class StaticResourceImageLoader implements ImageLoader {

    @Override
    public Image loadImage(URL url) throws IOException {
        byte[] bytes;
        try (InputStream in = url.openStream()) {
            bytes = in.readAllBytes();
        }
        String s = imageSizeAndMimeType(url.toString(), bytes);
        int width = Integer.parseInt(s.substring(0, s.indexOf(',')));
        s = s.substring(s.indexOf(',') + 1);
        int height = Integer.parseInt(s.substring(0, s.indexOf(',')));
        s = s.substring(s.indexOf(',') + 1);
        String mimeType = s;

        String dataURI = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        return new StaticResourceImageImpl(dataURI, 0, 0, width, (int) height);
    }

    private static native String imageSizeAndMimeType(String url, byte[] image);

    static class StaticResourceImageImpl extends Image {

        final String data;
        final int x;
        final int y;

        public StaticResourceImageImpl(String data, int x, int y, int width, int height) {
            super(width, height);
            this.data = data;
            this.x = x;
            this.y = y;
        }

        @Override
        protected void readDataImpl(int x, int y, int w, int h, MemorySegment dst) {
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public Image subimage(int x, int y, int w, int h) {
            if (x < 0 || y < 0 || x + w > this.width || y + h > this.height)
                throw new IllegalArgumentException();
            return new StaticResourceImageImpl(data, this.x + x, this.y + y, w, h);
        }
    }
}
 */