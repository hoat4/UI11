package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;
import ui11.control.PlainTextEditor;

public class HtmlEditableTextPeer extends HtmlPeerBase {

    private final PlainTextEditor plainTextEditor;

    public HtmlEditableTextPeer(PlainTextEditor plainTextEditor) {
        this.plainTextEditor = plainTextEditor;
    }

    @Override
    protected Widget build() {
        return htmlElement(this::writeImpl);
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        HTMLElementType wrapper = HTMLOutputUtil.beginElementOrWrap(HTMLElementType.INPUT, out, req);
        HTMLOutputUtil.writeClassAttr(req, out);
        HTMLOutputUtil.writeStyleAttr(req, out);
        out.writeByte('>');

        if (wrapper != null)
            wrapper.writeEndElement(out);
    }
}
