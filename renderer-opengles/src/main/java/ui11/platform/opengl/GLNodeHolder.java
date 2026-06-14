package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import ui11.EndingWidget;
import ui11.platform.opengl.inputtree.InputNode;
import ui11.platform.opengl.peer.GLOverlayPeer;
import ui11.platform.opengl.rendertree.RenderNode;
import ui11.resolution.PeerCreationRequest;

import java.util.Objects;

public final class GLNodeHolder extends EndingWidget {

    private final @NonNull RenderNode renderNode;
    private final @NonNull InputNode inputNode;

    public GLNodeHolder(@NonNull RenderNode renderNode, @NonNull InputNode inputNode) {
        this.renderNode = Objects.requireNonNull(renderNode);
        this.inputNode = Objects.requireNonNull(inputNode);
    }

    public @NonNull RenderNode renderNode() {
        return renderNode;
    }

    public @NonNull InputNode inputNode() {
        return inputNode;
    }

    public static class GLNodeRequest extends PeerCreationRequest<GLNodeHolder> {

        public GLNodeRequest() {
            super(GLNodeHolder.class);
        }
    }
}
