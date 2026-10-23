package ui11.output.html;

import ui11.Widget;
import ui11.output.html.util.UTF8Writer;

public class HtmlEmptyPeer extends HtmlPeerBase {

    public static final HtmlEmptyPeer INSTANCE = new HtmlEmptyPeer();

    private HtmlEmptyPeer() {
    }

    @Override
    protected Widget build() {
        return htmlElement(this::writeImpl);
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req) {
        HTMLOutputUtil.beginLayoutDisplayModeElement(out, req);
        HTMLOutputUtil.writeStyleAttr(req, out);
        HTMLOutputUtil.writeClassAttr(req, out);
        out.writeByte('>');
        HTMLOutputUtil.endLayoutDisplayModeElement(out, req);
    }
}
