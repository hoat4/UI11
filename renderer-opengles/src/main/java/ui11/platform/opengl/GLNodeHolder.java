package ui11.platform.opengl;

import org.jspecify.annotations.NonNull;
import ui11.renderer.input.InputNode;
import ui11.platform.opengl.rendertree.RenderNode;

import java.util.Objects;

public final class GLNodeHolder {

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
}
