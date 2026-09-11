package ui11.renderer.j2d.peer;

import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Vec2;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.FillPathNode;
import ui11.text.TextStyle;

import java.awt.*;

public class J2DLinearGradientPeer extends Widget {

    private final LinearGradient gradient;

    @Inject private J2DVisualContentRequest request;
    @Inject private Surface surface;
    @Inject private TextStyle textStyle;

    @Remember private FillPathNode node;

    public J2DLinearGradientPeer(LinearGradient gradient) {
        this.gradient = gradient;
    }

    @Override
    protected void initState() {
        node = new FillPathNode();
    }

    @Override
    protected Widget build() {
        Shape shape = surface.layoutShape();
        if (J2DUtil.isNotVisible(shape, surface))
            return request.createResponse(EmptyNode.INSTANCE);
        Rect bounds = shape.bounds(surface.coordinateSpace());

        float[] fractions = new float[gradient.stops().size()];
        Color[] colors = new Color[gradient.stops().size()];
        double emSize = textStyle.size();
        double deg = gradient.angleDeg();
        double w = bounds.width(), h = bounds.height();

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

        // EmptyNode?

        node.paint.set(paint);
        node.shape.set(J2DUtil.shapeToJ2D(shape, surface.coordinateSpace()));

        return request.createResponse(node);
    }
}
