package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.graphics.fill.ColorFill;
import ui11.platform.dom.DOMPeerBase;

public class ColorFillPeer extends DOMPeerBase<HTMLElement> {

    private final ColorFill colorFill;

    public ColorFillPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        elem().getStyle().setProperty("background-color", colorFill.color().toString());
        return endingWidget();
    }

    @Override
    protected String asCSSColor() {
        return colorFill.color().toString();
    }
}
