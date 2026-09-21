package ui.platform.glass;

import java.util.List;

class Frame<L> {

    public final L rootLayer;
    public final List<RenderDoneCallback> renderDoneCallbacks;
    final int viewportWidth, viewportHeight;

    public Frame(L rootLayer, List<RenderDoneCallback> renderDoneCallbacks,
                 int viewportWidth, int viewportHeight) {
        this.rootLayer = rootLayer;
        this.renderDoneCallbacks = renderDoneCallbacks;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public boolean isForAnimation() {
        return renderDoneCallbacks.isEmpty();
    }

    @Override
    public String toString() {
        return "Frame{" +
                "displayList=" + rootLayer +
                " (" +
                (isForAnimation() ? "non-resizing" : "for resize") + ")" +
                '}';
    }

    public interface RenderDoneCallback {

        void renderFinished();

        void willNotRender();
    }
}
