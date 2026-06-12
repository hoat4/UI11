package ui11.platform.dom;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import ui11.Widget;
import ui11.layout.singlechild.PassiveSize;
import ui11.platform.dom.HTMLCanvas.CanvasListener.ViewportSize;
import ui11.platform.dom.bindings.DOMRect;

// ha ezt használni akarjuk újra, akkor javítsuk ki hogy resize listener sosincs törölve.
// mondjuk amúgy is hülyeség hogy window resizekor nézzük a canvas méretét. 

public class HTMLCanvas extends Widget {

    private final CanvasListener listener;

    @Remember private HTMLCanvasElement canvas;

    public HTMLCanvas(CanvasListener listener) {
        this.listener = listener;
    }

    @Override
    protected void initState() {
        canvas = (HTMLCanvasElement) Window.current().
                getDocument().createElement("canvas");
        canvas.setWidth(500);
        canvas.setHeight(500);
    }

    @Override
    protected void onResume() {
        Window.requestAnimationFrame(timestamp -> {
            ViewportSize initialViewport = refreshCanvasSizeContinuously();
            listener.canvasReady(canvas, initialViewport);
        });
    }

    private ViewportSize refreshViewport() {
        var dpp = Window.current().getDevicePixelRatio();
        var rect = (DOMRect) canvas.getBoundingClientRect();
        double w = rect.getWidth();
        double h = rect.getHeight();
        var width = (int) Math.round(w * dpp);
        var height = (int) Math.round(h * dpp);
        // System.out.printf("w: %f, h: %f, width: %d, height: %d, \n", w * dpp, h * dpp, width, height);
        canvas.setWidth(width);
        canvas.setHeight(height);

        return new ViewportSize(width, height, dpp);
    }

    private ViewportSize refreshCanvasSizeContinuously() {
        ViewportSize viewport = refreshViewport();
        Window.current().addEventListener("resize", new EventListener<Event>() {
            private ViewportSize prev = viewport;

            @Override
            public void handleEvent(Event resizeEvt) {
                ViewportSize v = refreshViewport();
                if (!v.equals(prev)) {
                    prev = v;
                    listener.viewportChanged(v);
                }
            }
        });
        return viewport;
    }

    @Override
    protected Widget build() {
        return new PassiveSize(new DOMElementWidget(canvas));
    }

    public interface CanvasListener {

        void canvasReady(HTMLCanvasElement canvas, ViewportSize initialViewport);

        void viewportChanged(ViewportSize viewport);

        record ViewportSize(int width, int height, double dotsPerLogicalPixel) {}
    }
}
