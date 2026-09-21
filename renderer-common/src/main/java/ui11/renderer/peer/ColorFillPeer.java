package ui11.renderer.peer;

import ui11.Expose;
import ui11.Widget;
import ui11.geom.Shape;
import ui11.graphics.Surface;
import ui11.graphics.fill.ColorFill;
import ui11.renderer.layer.SolidFillShapeItem;
import ui11.renderer.layer.Item;

public class ColorFillPeer extends Widget {

    private final ColorFill colorFill;

    @Inject private Surface surface;
    @Inject private Item.ItemRequest request;

    public ColorFillPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
    }

    @Override
    protected Widget build() {
        Shape shape = surface.layoutShape(); // vagy clipShape?
        Item item;
        if (Shape.degenerateShape().equals(shape))
            item = Item.EMPTY;
        else
            item = new SolidFillShapeItem(shape, colorFill.color());
        return new Expose<>(request, item);
    }
}
