package ui11.output.html;

import ui11.Widget;
import ui11.layout.singlechild.Padding;
import ui11.output.html.util.UTF8Writer;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlPaddingPeer extends HtmlPeerBase {

    private static final byte[] CLASS_WRAPPERELEMENT = bytes(" class=\"we");

    private final Padding padding;

    public HtmlPaddingPeer(Padding padding) {
        this.padding = padding;
    }

    @Override
    protected Widget build() {
        return makePeer(padding.content(), childPeer ->
            htmlElement((out, req) -> writeImpl(out, req, childPeer)));
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req, WriteableHtmlElement childPeer) {
        HTMLElementType elementType = req.typeOrDiv();
        elementType.writeBeginElement(out);

        out.writeBytes(CLASS_WRAPPERELEMENT);
        HTMLOutputUtil.writeClassNames(req, out);
        out.writeByte('"');

        out.writeBytes(HTMLOutputUtil.STYLE_ATTR_BEGIN);
        HTMLOutputUtil.writePassedStyleProps(req, out);
        HTMLOutputUtil.writePadding(padding.insets(), out);
        out.writeBytes('"', '>');

        childPeer.write(out, new HTMLElementRequirements());
        elementType.writeEndElement(out);
    }
}
