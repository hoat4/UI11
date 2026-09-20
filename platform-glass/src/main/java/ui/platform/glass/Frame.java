package ui.platform.glass;

import java.util.List;

class Frame<D> {

    public final D displayList;
    public final List<RenderDoneCallback> renderDoneCallbacks;

    public Frame(D displayList, List<RenderDoneCallback> renderDoneCallbacks) {
        this.displayList = displayList;
        this.renderDoneCallbacks = renderDoneCallbacks;
    }

    public boolean isForAnimation() {
        return renderDoneCallbacks.isEmpty();
    }

    @Override
    public String toString() {
        return "Frame{" +
                "displayList=" + displayList +
                " (" +
                (isForAnimation() ? "non-resizing" : "for resize") + ")" +
                '}';
    }

    public interface RenderDoneCallback {

        void renderFinished();

        void willNotRender();
    }
}
