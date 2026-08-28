package ui11.platform.awt.j2d.peer;

import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DUtil;
import ui11.platform.awt.j2d.inputtree.OpaqueInputNode;
import ui11.platform.awt.j2d.inputtree.TransparentInputNode;
import ui11.platform.awt.j2d.rendertree.EmptyRenderNode;
import ui11.platform.awt.j2d.rendertree.FillPathRenderNode;
import ui11.text.TextStyle;

import java.awt.*;

public class J2DLinearGradientPeer extends Widget {

    private final LinearGradient gradient;
    private final J2DSurface surface;

    @Inject private TextStyle textStyle;

    @Remember private FillPathRenderNode node;
    @Remember private OpaqueInputNode inputNode;

    public J2DLinearGradientPeer(LinearGradient gradient, J2DSurface surface) {
        this.gradient = gradient;
        this.surface = surface;
    }

    @Override
    protected void initState() {
        node = new FillPathRenderNode();
        inputNode = new OpaqueInputNode();
    }

    @Override
    protected Widget build() {
        Shape shape = surface.shape();
        if (shape == J2DSurface.INFINITE_SHAPE)
            return surface.createResponse(new J2DNodeHolder(EmptyRenderNode.INSTANCE, TransparentInputNode.INSTANCE));

        float[] fractions = new float[gradient.stops().size()];
        Color[] colors = new Color[gradient.stops().size()];
        double emSize = textStyle.size();
        double deg = gradient.angleDeg();
        double w = surface.size().width(), h = surface.size().height();

        deg -= 90;
        if (deg < 0)
            deg = 360 + deg % 360;
        else
            deg %= 360;
        if (deg >= 180)
            deg = 360 - deg;
        if (deg >= 90)
            deg = 180 - deg;

        double b = Math.toRadians(deg);
        double l = Math.sin(b) * h + Math.cos(b) * w;

        for (int i = 0; i < gradient.stops().size(); i++) {
            Stop stop = gradient.stops().get(i);
            fractions[i] = (float) ((stop.pos().em() * emSize + stop.pos().px() + stop.pos().rel() * l) / l);
            colors[i] = J2DUtil.color(stop.color());
        }

        Vec2 s = Vec2.ofPolarRad(-Math.toRadians(gradient.angleDeg() - 90), l / 2);

        Paint paint = new LinearGradientPaint(
                (float) (w / 2 - s.x()), (float) (h / 2 - s.y()),
                (float) (w / 2 + s.x()), (float) (h / 2 + s.y()),
                fractions, colors);

        // EmptyRenderNode?

        node.paint.set(paint);
        node.shape.set(shape);
        inputNode.shape.set(shape);

        return surface.createResponse(new J2DNodeHolder(node, inputNode));
    }
}
