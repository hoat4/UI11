package ui11.renderer;

import ui11.graphics.VisualContentRequest;

public interface Renderer<N extends Node> {

    VisualContentRequest<N> createRootContentRequest(
            RenderableSurface surface);

    void render(N root);
}
