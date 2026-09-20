package ui11.platform.opengl.renderer.displaylist;

import java.util.ArrayList;
import java.util.List;

public class DisplayList {

    public final int viewportWidth, viewportHeight;
    public final List<DisplayListItem> items = new ArrayList<>();

    public DisplayList(int viewportWidth, int viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
    }

    @Override
    public String toString() {
        return "DisplayList (" + viewportWidth + "x" + viewportHeight + ", " + items.size() + " items)";
    }
}
