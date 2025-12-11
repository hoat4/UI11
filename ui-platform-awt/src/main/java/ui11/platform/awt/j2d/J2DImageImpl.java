package ui11.platform.awt.j2d;

import ui11.graphics.fill.RasterImage;
import java.lang.foreign.MemorySegment;

public class J2DImageImpl extends RasterImage {

    final java.awt.image.BufferedImage img;
    final int x, y;

    private J2DImageImpl(java.awt.image.BufferedImage img, int x, int y, int w, int h) {
        super(w, h);
        if (x < 0 || y < 0 || x + w > img.getWidth() || y + h > img.getHeight())
            throw new IllegalArgumentException();
        this.x = x;
        this.y = y;
        this.img = img;
    }

    // azért nem publikus konstruktor van, hogy lehessen csinálni később
    // nem-buffered image bemenetet is
    public static J2DImageImpl of(java.awt.image.BufferedImage image) {
        return new J2DImageImpl(image, 0, 0, image.getWidth(), image.getHeight());
    }

    @Override
    protected void readDataImpl(int x, int y, int w, int h, MemorySegment dst) {
        int[] pixels = img.getRGB(0, 0, w, h, (int[]) null, 0, w);
        dst.copyFrom(MemorySegment.ofArray(pixels));
    }

    @Override
    public RasterImage subimage(int x, int y, int w, int h) {
        if (x < 0 || y < 0 || x + w > this.width || y + h > this.height)
            throw new IllegalArgumentException();
        return new J2DImageImpl(img, this.x + x, this.y + y, w, h);
    }
}
