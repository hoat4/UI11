package ui11.output.html;

import ui11.Widget;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.output.html.util.UTF8Writer;

import static ui11.output.html.HTMLOutputUtil.bytes;

class HtmlColorFillPeer extends HtmlPeerBase {

    private static final byte[] BACKGROUND = bytes("background:");

    private final ColorFill colorFill;

    public HtmlColorFillPeer(ColorFill colorFill) {
        this.colorFill = colorFill;
    }

    @Override
    protected Widget build() {
        return htmlElement(this::writeImpl);
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        HTMLElementType elementType = req.typeOrDiv();
        elementType.writeBeginElement(out);
        HTMLOutputUtil.writeClassAttr(req, out);
        out.writeBytes(HTMLOutputUtil.STYLE_ATTR_BEGIN);
        HTMLOutputUtil.writePassedStyleProps(req, out);
        out.writeBytes(BACKGROUND);
        HTMLOutputUtil.writeColor(colorFill.color(), out);
        out.writeBytes('"', '>');
        elementType.writeEndElement(out);
    }

    @Override
    public Color asColor() {
        return colorFill.color();
    }
}
