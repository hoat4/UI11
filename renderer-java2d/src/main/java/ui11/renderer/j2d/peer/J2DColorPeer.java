package ui11.renderer.j2d.peer;

import ui11.Widget;
import ui11.graphics.fill.ColorFill;
import ui11.renderer.j2d.J2DVisualContentRequest;
import ui11.renderer.j2d.J2DUtil;
import ui11.renderer.j2d.rendertree.EmptyNode;
import ui11.renderer.j2d.rendertree.FillPathNode;

import java.awt.*;

public class J2DColorPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private J2DVisualContentRequest surface;

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
        Shape shape = surface.shape();

        if (shape == J2DVisualContentRequest.INFINITE_SHAPE)
            return surface.createResponse(EmptyNode.INSTANCE);

        // mivel input opaque-nak számít, ezért nem tudunk visszaadni EmptyNode-ot, ha a color==Color.TRANSPARENT

        Color awtColor = J2DUtil.color(colorFill.color());
        node.paint.set(awtColor);
        node.shape.set(shape);
        return surface.createResponse(node);
    }
}
