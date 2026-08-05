package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Slot;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.geom.Vec2;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

// TODO ha ez active element marad, de nincs a widget fában,
//      akkor meg kéne maradnia a scroll pozíciónak? DOMScrollablePeer esetén jelenleg eltűnik
public final class Scrollable extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @Nullable Runnable onScroll;
    private final @Nullable ScrollController scrollController;

    @Remember private Slot contentSlot;

    public Scrollable(@NonNull Widget content, @Nullable Runnable onScroll, @Nullable ScrollController scrollController) {
        this.content = Objects.requireNonNull(content);
        this.onScroll = onScroll; // TODO ez lehetne listener proxy
        this.scrollController = scrollController;
    }

    public Scrollable(Widget content) {
        this(content, null);
    }

    public Scrollable(Widget content, Runnable onScroll) {
        this(content, onScroll, null);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot();
    }

    @Override
    protected Scrollable forSubstitution() {
        return new Scrollable(contentSlot.with(content), onScroll, scrollController);
    }

    public @NonNull Widget content() {
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
