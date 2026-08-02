package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequestor;
import ui11.SubstitutedWidget;
import ui11.platform.opengl.inputtree.InputNode;
import ui11.platform.opengl.rendertree.RenderNode;

import java.util.Objects;

public final class GLNodeHolder extends SubstitutedWidget {

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

    public static class GLNodeRequest extends PeerRequestor.Request<GLNodeHolder> {

        public GLNodeRequest() {
            super(GLNodeHolder.class);
        }
    }
}
