package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLImageElement;
import ui11.document.URLImageView;
import ui11.platform.dom.DOMPeerBase;

public class URLImageViewPeer extends DOMPeerBase<HTMLElement> {

    private final URLImageView urlImageView;

    public URLImageViewPeer(URLImageView urlImageView) {
        this.urlImageView = urlImageView;
    }

    @Override
    protected String elementName() {
        return urlImageView.interactive() ? "object" : "img";
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected void update() {
        String src = urlImageView.url().toString();
        if (urlImageView.interactive())
            elem().setAttribute("data", src);
        else
            ((HTMLImageElement) elem()).setSrc(src);
    }
}
