/*
package ui11.output.html;

import ui11.output.html.util.UTF8Writer;
import ps.common.ui.FontIconDesc;

import static ui11.output.html.HTMLOutputUtil.bytes;

class HtmlFontIconPeer extends HtmlPeer {

    private static final byte[] CLASS_FONTICON = bytes(" class=\"gf");

    @Model private FontIconDesc e;

    @Override
    protected void update() {
    }

    @Override
    public void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        HTMLElementType elementType = req.typeOrDiv();
        elementType.writeBeginElement(out);

        HTMLOutputUtil.writeStyleAttr(req, out);

        out.writeBytes(CLASS_FONTICON);
        HTMLOutputUtil.writeClassNames(req, out);
        out.writeBytes('"', '>');

        if (e.codePoint() < 0x1000 || e.codePoint() > 0xFFFF)
            throw new RuntimeException("unsupported code point: " + e.codePoint());

        out.write(e.codePoint());

        elementType.writeEndElement(out);
    }
}
*/