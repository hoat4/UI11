package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlURLImageView extends HtmlPeerBase{

    private static final byte[] SRC = bytes(" src=\"");

    private final String sourceURL;

    public HtmlURLImageView(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    @Override
    protected Widget build() {
        return htmlElement(this::writeImpl);
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        HTMLElementType wrapper = HTMLOutputUtil.beginElementOrWrap(HTMLElementType.IMG, out, req);
        out.writeBytes(SRC);
        HTMLOutputUtil.attrValue(out, sourceURL);
        out.writeByte('"');
        HTMLOutputUtil.writeClassAttr(req, out);
        HTMLOutputUtil.writeStyleAttr(req, out);
        out.writeByte('>');

        if (wrapper != null)
            wrapper.writeEndElement(out);
    }
}
