package ui11.renderer.j2d.peer;

import ui11.Widget;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.graphics.fill.ColorFill;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.FillPathNode;

import java.awt.*;

public class J2DColorPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private Surface surface;
    @Inject private J2DVisualContentRequest request;

    @Remember private FillPathNode node;

    public J2DColorPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
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

        // mivel input opaque-nak számít, ezért nem tudunk visszaadni EmptyNode-ot, ha a color==Color.TRANSPARENT

        Color awtColor = J2DUtil.color(colorFill.color());
        node.paint.set(awtColor);
        node.shape.set(J2DUtil.shapeToJ2D(shape, surface.coordinateSpace()));
        return request.createResponse(node);
    }
}
