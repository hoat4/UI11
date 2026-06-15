package ui11.platform.awt.j2d.peer;

import ui11.Widget;
import ui11.graphics.Surface;
import ui11.graphics.fill.ColorFill;
import ui11.platform.awt.j2d.J2DNodeHolder;
import ui11.platform.awt.j2d.J2DSurface;
import ui11.platform.awt.j2d.J2DUtil;
import ui11.platform.awt.j2d.inputtree.OpaqueInputNode;
import ui11.platform.awt.j2d.inputtree.TransparentInputNode;
import ui11.platform.awt.j2d.rendertree.EmptyRenderNode;
import ui11.platform.awt.j2d.rendertree.FillPathRenderNode;

import java.awt.*;

public class J2DColorPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private Surface surface;

    @Remember private FillPathRenderNode node;
    @Remember private OpaqueInputNode inputNode;

    public J2DColorPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
    }

    @Override
    protected void initState() {
        node = new FillPathRenderNode();
        inputNode = new OpaqueInputNode();
    }

    @Override
    protected Widget build() {
        Shape shape = ((J2DSurface) surface).shape();

        if (shape == J2DSurface.INFINITE_SHAPE)
            return new J2DNodeHolder(EmptyRenderNode.INSTANCE, TransparentInputNode.INSTANCE);

        // TODO ezt observeli J2DGroupPeer isOpaque miatt, és valamiért invalidálni próbálja J2DGroupPeert ez,
        //      ezért exception lesz itt (pl. ButtonTest)
        inputNode.shape.set(shape);

        if (colorFill.color().equals(ui11.color.Color.TRANSPARENT))
            return new J2DNodeHolder(EmptyRenderNode.INSTANCE, inputNode);

        Color awtColor = J2DUtil.color(colorFill.color());
        node.paint.set(awtColor);
        node.shape.set(shape);
        return new J2DNodeHolder(node, inputNode);
    }
}
