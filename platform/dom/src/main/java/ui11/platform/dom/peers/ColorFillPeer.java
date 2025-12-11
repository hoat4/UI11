package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.graphics.fill.Color;
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
    protected void update() {
        elem().getStyle().setProperty("background-color", colorFill.color().toString());
    }

    @Override
    protected String asCSSColor() {
        return colorFill.color().toString();
    }
}
