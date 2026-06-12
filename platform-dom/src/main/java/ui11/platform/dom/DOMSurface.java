package ui11.platform.dom;

import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.control.Tooltip;
import ui11.geom.Location.CoordinateSpace;
import ui11.geom.Mat4;
import ui11.geom.Size;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.input.focus.FocusListener;
import ui11.input.pointer.WithCursor.Cursor;
import ui11.input.pointer.WithCursor.StandardCursor;
import ui11.input.pointer.PointerRegion;
import ui11.observable.ObserverHolder;
import ui11.platform.dom.DOMWidgetWrapper.ProxySurface;
import ui11.platform.dom.bindings.DOMRect;
import ui11.text.TextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DOMSurface implements Surface {

    private static final String CLASS_WRAPPED_TEXT = "tw";
    private static final String CLASS_NON_WRAPPED_TEXT = "w";

    final DOMEnvironment env;
    final HTMLElement element;

    private Set<String> prevCssClasses = Set.of();
    private final List<EventListener<?>> clickListeners = new ArrayList<>();
    private final List<EventListener<?>> focusListeners = new ArrayList<>();
    List<PointerRegion> pointerListeners = List.of();

    public DOMSurface(DOMEnvironment env, HTMLElement element) {
        this.env = env;
        this.element = element;

        env.window.setData(this.element, this);
        // htmlElement.setAttribute("data-re", toString());
    }

    @Override
    public CoordinateSpace coordinateSpace() {
        if (!ObserverHolder.hasNoObserver())
            throw new RuntimeException("TODO observer size changes");
        DOMRect rect = (DOMRect) element.getBoundingClientRect();
        //System.out.println("cs of "+element+": "+rect.getX()+","+rect.getY()+" "+rect.getWidth()+"x"+rect.getHeight());
        return new CoordinateSpace(env.clientCoordinateSpace,
                // TODO többi transformok
                Mat4.ofTranslation(new Vec2(rect.getX(), rect.getY())));
    }

    @Override
    public Size size() {
        if (!ObserverHolder.hasNoObserver())
            throw new RuntimeException("TODO observer size changes");
        DOMRect rect = (DOMRect) element.getBoundingClientRect();
        return new Size(rect.getWidth(), rect.getHeight());
    }

    @Override
    public double devicePixelRatio() {
        return env.window.getWindow().getDevicePixelRatio();
    }

    void update(ProxySurface proxySurface, CumulatingPropList cumulativePropList,
                TextStyle ts) {
        proxySurface.s = this;

        if (cumulativePropList.hidden())
            element.getStyle().setProperty("display", "none");
        else
            element.getStyle().removeProperty("display");

        prevCssClasses.forEach(c -> {
            if (!cumulativePropList.cssClasses().contains(c))
                element.getClassList().remove(c);
        });
        cumulativePropList.cssClasses().forEach(c -> element.getClassList().add(c));
        prevCssClasses = cumulativePropList.cssClasses();

        if (ts.size() != null)
            element.getStyle().setProperty("font-size", ts.size() + "px");
        else
            element.getStyle().removeProperty("font-size");

        if (ts.color() != null)
            element.getStyle().setProperty("color", ts.color().toString());
        else
            element.getStyle().removeProperty("color");

        if (ts.alignment() != null)
            element.getStyle().setProperty("text-align", switch (ts.alignment()) {
                case LEFT -> "left";
                case RIGHT -> "right";
                case CENTER -> "center";
                case JUSTIFY -> "justify";
            });
        else
            element.getStyle().removeProperty("text-align");

        if (ts.weight() != null)
            element.getStyle().setProperty("font-weight", switch (ts.weight()) {
                case NORMAL -> "normal";
                case BOLD -> "bold";
                case SEMI_BOLD -> "600";
                case HEAVY -> "900";
            });
        else
            element.getStyle().removeProperty("font-weight");

        if (ts.underline() != null)
            element.getStyle().setProperty("text-decoration", ts.underline() ? "underline" : "none");
        else
            element.getStyle().removeProperty("text-decoration");

        if (ts.lineHeight() != null)
            element.getStyle().setProperty("line-height", ts.lineHeight().toString());
        else
            element.getStyle().removeProperty("line-height");

        switch (ts.wrapping()) {
            case NEVER -> {
                element.getClassList().remove(CLASS_WRAPPED_TEXT);
                element.getClassList().add(CLASS_NON_WRAPPED_TEXT);
            }
            case EVERYWHERE -> {
                element.getClassList().add(CLASS_WRAPPED_TEXT);
                element.getClassList().remove(CLASS_NON_WRAPPED_TEXT);
            }
            case null -> {
                element.getClassList().remove(CLASS_NON_WRAPPED_TEXT);
                element.getClassList().remove(CLASS_WRAPPED_TEXT);
            }
            default -> {
                throw new RuntimeException("TODO " + ts.wrapping());
            }
        }

        if (ts.fontFamily() != null)
            element.getStyle().setProperty("font-family", ts.fontFamily());
        else
            element.getStyle().removeProperty("font-family");

        if (ts.letterSpacing() != null)
            element.getStyle().setProperty("letter-spacing", ts.letterSpacing().toString());
        else
            element.getStyle().removeProperty("letter-spacing");

        clickListeners.forEach(el -> element.removeEventListener("click", el));
        //System.out.println("cl: "+tmp_findTags(ClickListener.class, DOMPeer.class));
        for (Runnable clickListener : cumulativePropList.onClick()) {
            EventListener<MouseEvent> el = env.wrapEventListener(evt -> clickListener.run(), this);
            clickListeners.add(el);
            element.addEventListener("click", el);
        }

        focusListeners.forEach(el -> {
            element.removeEventListener("focusin", el);
            element.removeEventListener("focusout", el);
        });
        for (FocusListener focusListener : cumulativePropList.onFocus()) {
            EventListener<MouseEvent> el1 = env.wrapEventListener(evt -> focusListener.onFocused().run(), this);
            EventListener<MouseEvent> el2 = env.wrapEventListener(evt -> focusListener.onFocusLost().run(), this);
            focusListeners.add(el1);
            focusListeners.add(el2);
            element.addEventListener("focusin", el1);
            element.addEventListener("focusout", el2);
        }

        pointerListeners = cumulativePropList.pointerRegions();

        String cursor = null;
        for (Cursor c : cumulativePropList.cursors()) {
            if (c instanceof StandardCursor standardCursor) {
                cursor = switch (standardCursor) {
                    case ARROW -> "default";
                    case HAND -> "pointer";
                    case TEXT -> "text";
                };
            } else {
                // TODO ilyenkor nem kéne nullra állítani?
            }
        }
        element.getStyle().setProperty("cursor", cursor);

        // TODO kéne reagálni tudni ezek megváltozására
        List<Tooltip> tooltipTags = cumulativePropList.tooltipTags();
        if (tooltipTags.isEmpty())
            element.removeAttribute("title");
        else
            element.setAttribute("title", tooltipTags.getLast().tooltip());
    }
}
