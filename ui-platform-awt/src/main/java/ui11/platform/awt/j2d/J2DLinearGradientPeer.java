package ui11.platform.awt.j2d;

import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.fill.LinearGradient;
import ui11.graphics.fill.LinearGradient.Stop;
import ui11.observable.Observable;
import ui11.provide.UpValueWrapper;
import ui11.text.TextStyle;

import java.awt.*;

public class J2DLinearGradientPeer extends Widget {

    private final LinearGradient gradient;

    @Inject private Observable<Surface> surface;
    @Inject private Observable<TextStyle> textStyle;

    @State private J2DLinearGradientPeerImpl state;

    public J2DLinearGradientPeer(LinearGradient gradient) {
        this.gradient = gradient;
    }

    @Override
    protected void initState() {
        state = new J2DLinearGradientPeerImpl();
    }

    @Override
    protected Widget build() {
        TextStyle textStyle = this.textStyle.get();
        Surface surface = this.surface.get();

        float[] fractions = new float[gradient.stops().size()];
        Color[] colors = new Color[gradient.stops().size()];
        double emSize = textStyle.size(); // TODO merge?
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

        state.p = new LinearGradientPaint(
                (float) (w / 2 - s.x()), (float) (h / 2 - s.y()),
                (float) (w / 2 + s.x()), (float) (h / 2 + s.y()),
                fractions, colors);

        // TODO ezt nem kéne mindig meghívni
        ((J2DSurface) surface).requestRepaint();
        return new UpValueWrapper(state);
    }

    private static class J2DLinearGradientPeerImpl implements J2DPrimitive {

        Paint p;

        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
            g.setPaint(p);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            return null;
        }
    }
}
