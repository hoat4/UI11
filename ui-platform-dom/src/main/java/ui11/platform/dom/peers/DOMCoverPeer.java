package ui11.platform.dom.peers;

import org.teavm.jso.dom.html.HTMLElement;
import ui11.Slot;
import ui11.Widget;
import ui11.layout.singlechild.Cover;
import ui11.platform.dom.DOMPeerBase;
import ui11.provide.Provider;
import ui11.provide.UpValue;

import java.net.URI;

public class DOMCoverPeer extends DOMPeerBase<HTMLElement> {

    private final Cover cover;

    @Inject private Slot contentSlot;

    public DOMCoverPeer(Cover cover) {
        this.cover = cover;
    }

    @Override
    protected void initElement() {
        elem().getStyle().setProperty("background-size", "cover");
        elem().getStyle().setProperty("background-position", "center");
    }

    @Override
    protected void update() {
        Widget wrappedContent = new Provider<>(CSSBackgroundImageContext.class,
                CSSBackgroundImageContext.BACKGROUND_IMAGE_CONTEXT,
                cover.content());
        CSSBackgroundImage img = contentSlot.instantiate(wrappedContent).lookup(CSSBackgroundImage.class);
        elem().getStyle().setProperty("background-image", "url(" + img.uri.toString() + ")");
    }

    public enum CSSBackgroundImageContext {
        BACKGROUND_IMAGE_CONTEXT
    }

    public static record CSSBackgroundImage(URI uri) implements UpValue {}
}
