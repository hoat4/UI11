package ui11.renderer.j2d.peer;

/*
import ui11.LayoutContext1;
import ui11.geom.Size;
import ui10.graphics.Opacity;
import ui10.layout.BoxConstraints;

import java.awt.*;

import static java.awt.AlphaComposite.SRC_OVER;


public class J2DOpacityElement extends J2DPrimitive<Opacity> {

    private J2DPrimitive<?> contentItem;

    public J2DOpacityElement(RenderingContext renderer, Opacity node) {
        super(renderer);
    }

    @Override
    protected void validateImpl() {
        if (contentItem == null) {
            node.content.initParent(this);
            contentItem = (J2DPrimitive<?>) node.content.renderableElement();
        }
    }

    @Override
    protected void drawImpl(Graphics2D g) {
        Composite prevComposite = g.getComposite();

        // should multiply with previous alpha, not replace
        g.setComposite(AlphaComposite.getInstance(SRC_OVER,node.fraction.toFloat()));
        contentItem.draw(g);
        g.setComposite(prevComposite);
    }

    @Override
    protected Size preferredSizeImpl(BoxConstraints constraints, LayoutContext1 context) {
        return constraints.min();
    }
}
*/