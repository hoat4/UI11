package ui11.platform.dom.peers;

import org.jspecify.annotations.NonNull;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.PeerRequest;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.layout.singlechild.Cover;
import ui11.platform.dom.DOMPeerBase;

import java.net.URI;
import java.util.Objects;

public class DOMCoverPeer extends DOMPeerBase<HTMLElement> {

    private final Cover cover;

    public DOMCoverPeer(Cover cover) {
        this.cover = cover;
    }

    @Override
    protected void initElement() {
        elem().getStyle().setProperty("background-size", "cover");
        elem().getStyle().setProperty("background-position", "center");
    }

    @Override
    protected Widget doBuild() {
        CSSBackgroundImagePeerCreationRequest req = new CSSBackgroundImagePeerCreationRequest();
        return PeerRequest.requestSingle(cover.content(), req, result -> {
            elem().getStyle().setProperty("background-image", "url(" + result.peer().uri.toString() + ")");
            return endingWidget();
        });
    }

    public static final class CSSBackgroundImage extends SubstitutedWidget {

        private final URI uri;

        public CSSBackgroundImage(@NonNull URI uri) {
            this.uri = Objects.requireNonNull(uri);
        }

        public @NonNull URI uri() {
            return uri;
        }
    }
}
