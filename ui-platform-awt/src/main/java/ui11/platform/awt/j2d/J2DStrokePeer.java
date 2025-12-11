package ui11.platform.awt.j2d;

import ui11.Slot;
import ui11.Widget;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.graphics.effect.Stroke;
import ui11.observable.Observable;
import ui11.provide.UpValueWrapper;

import java.awt.*;

public class J2DStrokePeer extends Widget {

    private final Stroke stroke;

    @Inject private Observable<Surface> parentSurface;
    @Inject private Slot textureSlot;

    @State private J2DStrokeImpl state;

    public J2DStrokePeer(Stroke stroke) {
        this.stroke = stroke;
    }

    @Override
    protected void initState() {
        state = new J2DStrokeImpl();
    }

    @Override
    protected Widget build() {
        state.awtShape = J2DUtil.pathToJ2D(stroke.path());

        ((J2DSurface) parentSurface.get()).requestRepaint();
        state.child = textureSlot.instantiate(stroke.texture()).lookup(J2DPrimitive.class);
        if (state.child instanceof J2DColorPrimitive(Color awtColor))
            state.awtColor = awtColor;
        else
            throw new RuntimeException("TODO stroke with non-solid fill: " + state.child);

        return new UpValueWrapper(state);
    }

    private static class J2DStrokeImpl implements J2DPrimitive {
        private Shape awtShape;
        private J2DPrimitive child;
        private Color awtColor;
        private java.awt.Stroke awtStroke = new BasicStroke();


        @Override
        public void draw(Graphics2D g, Rectangle bounds) {
            g.setColor(awtColor);
            g.setStroke(awtStroke);
            g.draw(awtShape);
        }

        @Override
        public PickResult findInputRegion(Vec2 p) {
            if (awtStroke.createStrokedShape(awtShape).contains(p.x(), p.y()))
                return child.findInputRegion(p);
            else
                return null;
        }
    }
}
