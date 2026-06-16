package ui11.platform.dom.peers;

import ui11.Widget;
import ui11.geom.Vec2;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.layout.singlechild.Scrollable;
import ui11.layout.singlechild.Scrollable.ScrollablePeer;
import ui11.platform.dom.*;

// TODO ez most nem jó, mert ha nem rakjuk PassiveSize-ba, akkor "kitolja" az alatta/jobbra lévő elemeket a képernyőből

public class DOMScrollablePeer extends DOMLayoutPeerBase {

    private static final String CLASS_OVERFLOW_SCROLL = "oS";

    private final Scrollable scrollable;

    public DOMScrollablePeer(Scrollable scrollable) {
        super(false, false);
        this.scrollable = scrollable;
    }

    @Override
    protected void initElement() {
        elem().getClassList().add(CLASS_OVERFLOW_SCROLL);
        elem().getClassList().add(DOMOverlayLayoutPeer.CLASS_SYMMETRIC_OVERLAY_GRID);
    }

    @Override
    protected Widget doBuild() {
        untilNextRebuild().onClose(elem().onEvent("scroll", evt -> {
            if (scrollable.onScroll() != null)
                scrollable.onScroll().run();
        })::dispose);

        return updateToSingleChild(scrollable.content());
    }

    @Override
    protected Widget wrapResult(DOMElementHolder h) {
        if (scrollable.scrollController() != null) {
            scrollable.scrollController().peer = new DOMScrollablePeerImpl(env(), h);
        }
        return super.wrapResult(h);
    }

    @Override
    protected boolean mouseTransparent() {
        return false;
    }

    private record DOMScrollablePeerImpl(DOMEnvironment env, DOMElementHolder h) implements ScrollablePeer {

        @Override
        public Rect viewedAreaOfContent() {
            return new Rect(
                    env.window.getScrollLeft(h.element()), env.window.getScrollTop(h.element()),
                    env.window.getOffsetWidth(h.element()), env.window.getOffsetHeight(h.element()));
        }

        @Override
        public Size contentSize() {
            return new Size(env.window.getScrollWidth(h.element()), env.window.getScrollHeight(h.element()));
        }

        @Override
        public void scrollTo(Vec2 p) {
            env.window.setScrollPos(h.element(), p.x(), p.y());
        }
    }
}
