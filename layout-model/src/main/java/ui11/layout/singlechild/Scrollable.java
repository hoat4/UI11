package ui11.layout.singlechild;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.Vec2;

import org.jspecify.annotations.Nullable;

// TODO ha ez active element marad, de nincs a widget fában,
//      akkor meg kéne maradnia a scroll pozíciónak? DOMScrollablePeer esetén jelenleg eltűnik
public final class Scrollable extends SubstitutedWidget {

    private final Widget content;
    @Nullable private final Runnable onScroll;
    @Nullable private final ScrollController scrollController;

    public Scrollable(Widget content, @Nullable Runnable onScroll, @Nullable ScrollController scrollController) {
        this.content = content;
        this.onScroll = onScroll;
        this.scrollController = scrollController;
    }

    public Scrollable(Widget content) {
        this(content, null);
    }

    public Scrollable(Widget content, Runnable onScroll) {
        this(content, onScroll, null);
    }

    public Widget content() {
        return content;
    }

    @Nullable
    public Runnable onScroll() {
        return onScroll;
    }

    @Nullable
    public ScrollController scrollController() {
        return scrollController;
    }

    @Override
    public String toString() {
        return "Scrollable[" +
                "content=" + content + ", " +
                "onScroll=" + onScroll + ", " +
                "scrollController=" + scrollController + ']';
    }


    public static class ScrollController {

        public ScrollablePeer peer; // TODO

        public Rect viewedAreaOfContent() {
            return peer.viewedAreaOfContent();
        }

        public Size contentSize() {
            return peer.contentSize();
        }

        public void scrollTo(Vec2 p) {
            peer.scrollTo(p);
        }
    }

    public interface ScrollablePeer {

        Rect viewedAreaOfContent();

        Size contentSize();

        void scrollTo(Vec2 p);
    }
}
