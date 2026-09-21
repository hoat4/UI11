package ui11.renderer.peer;

import ui11.Expose;
import ui11.Widget;
import ui11.color.Color;
import ui11.geom.Shape;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.LinearGradientFillShapeItem;
import ui11.text.TextStyle;

public class LinearGradientPeer extends Widget {

    private final LinearGradient gradient;

    @Inject private Surface surface;
    @Inject private Item.ItemRequest request;
    @Inject private TextStyle textStyle;

    public LinearGradientPeer(LinearGradient gradient) {
        this.gradient = gradient;
    }

    @Override
    protected Widget build() {
        Shape shape = surface.layoutShape();
        if (Shape.degenerateShape().equals(shape))
            return new Expose<>(request, Item.EMPTY);

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

        double angleRad = Math.toRadians(deg);
        double gradientLengthPX = Math.sin(angleRad) * h + Math.cos(angleRad) * w;

        int stopCount = gradient.stops().size();
        Vec2[] lineStarts = new Vec2[stopCount];
        Color[] colors = new Color[stopCount];
        Vec2 direction = Vec2.ofPolarRad(-Math.toRadians(gradient.angleDeg() - 90), 1);
        for (int i = 0; i < stopCount; i++) {
            Stop stop = gradient.stops().get(i);
            double posInPX = stop.pos().em() * emSize + stop.pos().px() + stop.pos().rel() * gradientLengthPX;
            lineStarts[i] = direction.mul(posInPX);
            colors[i] = stop.color();
        }

        return new Expose<>(request, new LinearGradientFillShapeItem(shape, direction, lineStarts, colors));
    }

}
