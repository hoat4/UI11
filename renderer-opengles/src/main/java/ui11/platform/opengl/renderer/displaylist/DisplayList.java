package ui11.platform.opengl.renderer.displaylist;

import java.util.ArrayList;
import java.util.List;

public class DisplayList {

    public final int viewportWidth, viewportHeight;
    public final List<DisplayListItem> items = new ArrayList<>();
    public final List<RenderDoneCallback> renderDoneCallbacks = new ArrayList<>();

    public DisplayList(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    public boolean isForAnimation() {
        return renderDoneCallbacks.isEmpty();
    }

    @Override
    public String toString() {
        return viewportWidth + "x" + viewportHeight + " (" +
                (isForAnimation() ? "non-resizing" : "for resize") + ")";
    }

    public interface RenderDoneCallback {

        void renderFinished();

        void willNotRender();
    }
}
