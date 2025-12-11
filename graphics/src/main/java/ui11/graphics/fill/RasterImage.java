package ui11.graphics.fill;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.net.URL;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Raszteres kép platformspecifikus reprezentációja.
 */
public abstract class RasterImage {

    // mivel egész számok a méretek, ezért nem lehet vektoros kép.
    // mivel csak egy méret van, nem lehet multi-resolution image.
    public final int width;
    public final int height;

    public RasterImage(int width, int height) {
        if (width < 0 || height < 0)
            throw new IllegalArgumentException();
        this.width = width;
        this.height = height;
    }

    public final void readData(int x, int y, int w, int h, MemorySegment dst) {
        if (x < 0 || x + w > width || h < 0 || y + h > height || dst.byteSize() != w * h * 4)
            throw new IllegalArgumentException();
        readDataImpl(x, y, w, h, dst);
    }

    protected abstract void readDataImpl(int x, int y, int w, int h, MemorySegment dst);

    public abstract RasterImage subimage(int x, int y, int w, int h);

    /**
     * Betölt egy képet a megadott URL-ről a {@link ImageLoader platformspecikus képbetöltő} használatával.
     */
    public static RasterImage load(URL url) throws IOException {
        Objects.requireNonNull(url);
        final ServiceLoader<ImageLoader> loaders = ServiceLoader.load(ImageLoader.class);
        for (ImageLoader l : loaders) {
            RasterImage i = l.loadImage(url);
            if (i != null)
                return i;
        }
        throw new RuntimeException("couldn't load image " + url +
                "\nProviders: " + loaders.stream().map(p -> p.type().getName()).toList());
    }

    /**
     * Olyan kép, mely egy {@linkplain MemorySegment MemorySegmentben} tárolja a nyers pixeleket.
     */
    public static class BufferedImage extends RasterImage {

        public final MemorySegment data;
        public final int x, y, fullImageWidth, fullImageHeight;

        public BufferedImage(int width, int height, MemorySegment data) {
            this(0, 0, width, height, width, height, data);
        }

        public BufferedImage(int x, int y, int width, int height,
                             int fullImageWidth, int fullImageHeight, MemorySegment data) {
            super(width, height);
            this.x = x;
            this.y = y;
            this.fullImageWidth = fullImageWidth;
            this.fullImageHeight = fullImageHeight;
            this.data = data;
            assert this.data.byteSize() == fullImageWidth * fullImageHeight * 4 : this.data.byteSize() + ", " + width + ", " + height;
        }

        @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
        @Override
        protected void readDataImpl(int x, int y, int w, int h, MemorySegment dst) {
            x += this.x;
            y += this.y;
            if (w == fullImageWidth)
                if (h == fullImageHeight)
                    dst.copyFrom(data);
                else
                    dst.copyFrom(data.asSlice(y * w * 4, w * h * 4));
            else
                for (int i = 0; i < h; i++)
                    dst.asSlice(i * w).copyFrom(data.asSlice((x + y * fullImageWidth) * 4, w * 4));
        }

        @Override
        public RasterImage subimage(int x, int y, int w, int h) {
            return new BufferedImage(this.x + x, this.y + y, w, h, fullImageWidth, fullImageHeight, data);
        }
    }
}
