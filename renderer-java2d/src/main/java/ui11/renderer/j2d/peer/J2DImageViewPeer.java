/*
package ui11.renderer.j2d;

import ui11.layout.protocol.SizingProvider;
import ui11.geom.Axis;
import ui11.geom.Rect;
import ui11.geom.Shape;
import ui11.geom.Size;
import ui11.layout.protocol.Sizing;

import java.lang.foreign.MemorySegment;

import java.awt.*;
import java.awt.image.BufferedImage;

public class J2DImagePeer extends J2DPrimitive implements SizingProvider {

    private BufferedImage image;
    private Rectangle imageClip;

    public J2DImagePeer(RenderingContext renderer) {
        super(renderer);
    }

    @Override
    protected boolean trySetModel(Object e) {
        if (e instanceof ui11.graphics.Image image1) {
            if (image1 instanceof J2DImageImpl i) {
                image = i.img;
                if (i.width != i.img.getWidth() || i.height != i.img.getHeight()) {
                    imageClip = new Rectangle(i.x, i.y, i.width, i.height);
                } else {
                    imageClip = null;
                }
            } else {
                BufferedImage image = new BufferedImage(image1.width, image1.height, BufferedImage.TYPE_INT_ARGB);
                int[] data = new int[image1.width * image1.height];
                image1.readData(0, 0, image1.width, image1.height, MemorySegment.ofArray(data));
                image.setRGB(0, 0, image1.width, image1.height, data, 0, image1.width);
                this.image = image;
                imageClip = null;
            }
            return true;
        } else
            return false;
    }

    @Override
    protected void drawImpl(Graphics2D g, Shape shape) {
        Rect b = shape.bounds();
        if (imageClip == null)
            g.drawImage(image,
                    (int) b.origin().x(), (int) b.origin().y(),
                    (int) b.size().width(), (int) b.size().height(),
                    null);
        else
            g.drawImage(image,
                    (int) b.topLeft().x(), (int) b.topLeft().y(),
                    (int) b.bottomRight().x(), (int) b.bottomRight().y(),
                    imageClip.x, imageClip.y,
                    imageClip.x + imageClip.width, imageClip.y + imageClip.height,
                    null);
    }

    // TODO ennek nem itt kéne lennie
    @Override
    public Sizing sizing() {
        return new Sizing() {

            private int w() {
                return imageClip == null ? image.getWidth() : imageClip.width;
            }

            private int h() {
                return imageClip == null ? image.getHeight() : imageClip.height;
            }

            @Override
            public Size preferredSize() {
                return new Size(w(), h());
            }

            @Override
            public double preferredSize(Axis axis, double crossAxisFixedLength) {
                return switch (axis) {
                    case HORIZONTAL -> crossAxisFixedLength / h() * w();
                    case VERTICAL -> crossAxisFixedLength / w() * h();
                };
            }
        };
    }

    @Override
    protected String toStringImpl() {
        return image.getWidth() + "×" + image.getHeight();
    }
}
*/