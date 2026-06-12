package ui11.platform.awt.j2d;

import ui11.EndingWidget;
import ui11.platform.awt.j2d.inputtree.InputNode;
import ui11.platform.awt.j2d.rendertree.RenderNode;

import org.jspecify.annotations.NonNull;
import java.util.Objects;

public final class J2DNodeHolder extends EndingWidget {

    private final @NonNull RenderNode renderNode;
    private final @NonNull InputNode inputNode;

    public J2DNodeHolder(@NonNull RenderNode renderNode, @NonNull InputNode inputNode) {
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
