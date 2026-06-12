package ui11.platform.dom.peers;

import org.jspecify.annotations.NonNull;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.EndingWidget;
import ui11.Slot;
import ui11.layout.singlechild.Cover;
import ui11.platform.dom.DOMPeerBase;

import java.net.URI;
import java.util.Objects;

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
        CSSBackgroundImage img = makePeer(contentSlot, cover.content(), new CSSBackgroundImagePeerCreationRequest());
        elem().getStyle().setProperty("background-image", "url(" + img.uri.toString() + ")");
    }

    public static final class CSSBackgroundImage extends EndingWidget {

        private final URI uri;

        public CSSBackgroundImage(@NonNull URI uri) {
            this.uri = Objects.requireNonNull(uri);
        }

        public @NonNull URI uri() {
            return uri;
        }
    }
}
