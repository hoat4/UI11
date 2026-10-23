package ui11.output.html;

import ui11.Widget;
import ui11.graphics.effect.Overlay;
import ui11.output.html.util.UTF8Writer;

import java.util.List;

import static ui11.output.html.HTMLOutputUtil.bytes;

public class HtmlOverlayLayoutPeer extends HtmlPeerBase {

    private static final byte[] CLASS = bytes("class=\"gQ");

    private final Overlay overlayLayout;

    public HtmlOverlayLayoutPeer(Overlay overlayLayout) {
        this.overlayLayout = overlayLayout;
    }

    @Override
    protected Widget build() {
        return makePeers(overlayLayout.items(), childrenPeers -> {
            return htmlElement((out, req) ->
                writeImpl(out, req, childrenPeers));
        });
    }

    private void writeImpl(UTF8Writer out, HTMLElementRequirements req,
                           List<? extends WriteableHtmlElement> childrenPeers) {
        if (overlayLayout.items().size() == 1) {
            childrenPeers.getFirst().write(out, req);
            return;
        }

        HTMLOutputUtil.beginLayoutDisplayModeElement(out, req);
        HTMLOutputUtil.writeStyleAttr(req, out);
        out.writeBytes(CLASS);
        HTMLOutputUtil.writeClassNames(req, out);
        out.writeBytes('"', '>');

        for (WriteableHtmlElement item : childrenPeers)
            item.write(out, new HTMLElementRequirements());

        HTMLOutputUtil.endLayoutDisplayModeElement(out, req);
    }
}
