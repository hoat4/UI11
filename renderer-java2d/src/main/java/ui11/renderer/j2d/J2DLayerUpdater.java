package ui11.renderer.j2d;

import ui11.color.Color;
import ui11.geom.Location;
import ui11.geom.Mat4;
import ui11.geom.Shape;
import ui11.geom.Vec2;
import ui11.renderer.TextRenderer;
import ui11.renderer.layer.Layer;
import ui11.renderer.subsurface.StrokedShape;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class J2DLayerUpdater implements Layer.LayerUpdater {

    private final J2DLayer j2DLayer;
    private final List<Layer> sublayers = new ArrayList<>();
    private final Location.CoordinateSpace coordinateSpace;

    private final Image image;
    public final Graphics2D g;

    public J2DLayerUpdater(J2DLayer j2dLayer) {
        this.j2DLayer = j2dLayer;
        this.coordinateSpace = j2dLayer.coordinateSpace();

        BufferedImage img = new BufferedImage(j2dLayer.bufferSize.width(), j2dLayer.bufferSize.height(),
                BufferedImage.TYPE_INT_ARGB);
        image = img;
        g = img.createGraphics();
        initRenderingHints(g);
    }

    private void initRenderingHints(Graphics2D g) {
        // TODO text antialiasing borzalmas ronda lett mióta nem LCD, de nem értem hogy miért

        Map<?, ?> desktopHints = (Map<?, ?>) Toolkit.getDefaultToolkit().
                getDesktopProperty("awt.font.desktophints");

        if (desktopHints != null) {
            g.setRenderingHints(desktopHints);
        } else {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    @Override
    public void fill(Shape shape, Color color) {
        java.awt.Color awtColor = J2DUtil.color(color);
        if (shape instanceof StrokedShape strokedShape) {
            java.awt.Shape awtShape = J2DUtil.pathToJ2D(strokedShape.path.transform(
                    strokedShape.coordinateSpace.transformationTo(coordinateSpace)));
            g.setColor(awtColor);
            g.setStroke(new BasicStroke((float) strokedShape.thickness));
            g.draw(awtShape);
        } else {
            java.awt.Shape awtShape = J2DUtil.shapeToJ2D(shape, coordinateSpace);
            g.setColor(awtColor);
            g.fill(awtShape);
        }
    }

    @Override
    public void fillWithLinearGradient(Shape shape, Vec2 direction, Vec2[] lineStarts, Color[] colors) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void fill(Shape shape, Layer sublayer) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void blend(double opacity, Layer sublayer) {
        throw new RuntimeException("TODO");
    }

    @Override
    public void text(TextRenderer.TextLayout text, Location.CoordinateSpace cs) {
        J2DTextLayoutCalculator.J2DTextLayout l = (J2DTextLayoutCalculator.J2DTextLayout) text;
        g.setColor(l.color);
        g.setFont(l.font);

        AffineTransform t = transformation(cs);
        double x = 0, y = l.maxAscent;
        if ((t.getType() & ~AffineTransform.TYPE_TRANSLATION) == 0) {
            x += t.getTranslateX();
            y += t.getTranslateY();
            g.drawString(l.text, (float) x, (float) y);
        } else {
            AffineTransform prevTransform = g.getTransform();
            g.transform(t);
            g.drawString(l.text, (float) x, (float) y);
            g.setTransform(prevTransform);
        }
    }

    AffineTransform transformation(Location.CoordinateSpace cs) {
        // TODO 3D esetén kéne fallback (pl. SVG-hez), nem eldobni a maradék mátrixelemeket.
        //      pl. szoftveresen elvégezni a perspektív osztást
        Mat4 t = cs.transformationTo(coordinateSpace);
        AffineTransform tx = new AffineTransform();
        tx.setTransform(
                t.m00(), t.m01(),
                t.m10(), t.m11(),
                t.m30(), t.m31()
        );
        return tx;
    }

    @Override
    public List<Layer> finish() {
        g.dispose();
        j2DLayer.content.set(image);
        return sublayers;
    }
}
