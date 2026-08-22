package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLVideoElement;
import ui11.Widget;
import ui11.media.Video;
import ui11.platform.dom.DOMPeerBase;

public class DOMVideoPeer extends DOMPeerBase<HTMLVideoElement> {

    private final Video video;

    public DOMVideoPeer(Video video) {
        this.video = video;
    }

    @Override
    protected String elementName() {
        return "video";
    }

    @Override
    protected void initElement() {
    }

    @Override
    protected Widget doBuild() {
        HTMLVideoElement htmlElement = elem();

        htmlElement.setSrc(video.source().toString());
        htmlElement.setLoop(video.loop());

        htmlElement.setAttribute("playsinline", "playsinline");
        htmlElement.setAttribute("webkit-playsinline", "webkit-playsinline");

        if (video.resolution() == null) {
            htmlElement.removeAttribute("width");
            htmlElement.removeAttribute("height");
        } else {
            htmlElement.setAttribute("width", Integer.toString(video.resolution().width()));
            htmlElement.setAttribute("height", Integer.toString(video.resolution().height()));
        }

        htmlElement.setAutoplay(true); // mivel nincs API-nk lejátszás indítására
        htmlElement.setMuted(true); // különben autoplay nem műnödik

        return endingWidget();
    }
}
