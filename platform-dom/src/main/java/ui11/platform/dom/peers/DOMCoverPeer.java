package ui11.platform.dom.peers;

import org.jspecify.annotations.NonNull;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.ExposeRequest;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.graphics.Surface;
import ui11.layout.singlechild.Cover;
import ui11.platform.dom.DOMPeerBase;

import java.net.URI;
import java.util.Objects;

public class DOMCoverPeer extends DOMPeerBase<HTMLElement> {

    private final Cover cover;

    @Inject private Surface inheritedSurface;

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
        // TODO egyelőre az inheritedSurface-t használjuk
        CSSBackgroundImagePeerCreationRequest req = new CSSBackgroundImagePeerCreationRequest(inheritedSurface);
        return ExposeRequest.requestSingle(cover.content(), req, result -> {
            elem().getStyle().setProperty("background-image", "url(" + result.uri.toString() + ")");
            return endingWidget();
        });
    }

    public static final class CSSBackgroundImage {

        private final URI uri;

        public CSSBackgroundImage(@NonNull URI uri) {
            this.uri = Objects.requireNonNull(uri);
        }

        public @NonNull URI uri() {
            return uri;
        }
    }
}
