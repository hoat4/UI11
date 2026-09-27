package ui.platform.glass;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.View;
import com.sun.glass.ui.Window;
import ui.platform.glass.windows.CompositorTimingThread;
import ui11.ExposeRequest;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.WidgetTree;
import ui11.animation.Scheduler;
import ui11.color.Color;
import ui11.observable.MutableObservable;
import ui11.provide.Provider;
import ui11.renderer.Renderer;
import ui11.renderer.layer.Item;
import ui11.renderer.layer.Layer;
import ui11.text.TextAlign;
import ui11.text.TextStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static ui11.geom.Length.px;

public class WindowImpl {

    public static final TextStyle DEFAULT_TEXT_STYLE = new TextStyle(
            Color.BLACK, 12D, "vacak",
            TextAlign.LEFT, TextStyle.FontWeight.NORMAL,
            TextStyle.Wrapping.NEVER, false, px(12),
            null /* TODO */, TextStyle.FontStyle.NORMAL
    );

    public final Application glassApp;
    private final Window window;
    private final Widget rootWidget;
    private View view;

    private final SchedulerImpl<?> scheduler = new SchedulerImpl<>();
    final PaintThread paintThread;

    /**
     * UI szálból írjuk és olvassuk
     */
    private final List<Frame.RenderDoneCallback> executeNextPaintTaskOnPlatformThread = new ArrayList<>();

    final MutableObservable<ViewSize> innerSize = MutableObservable.withInitial(new ViewSize(300, 300));
    private final GlassSurface rootSurface;

    private final CompositorTimingThread compositionTimingThread;
    private final Item.ItemRequest rootContentRequest;
    private final MutableObservable<Layer> rootLayer = MutableObservable.ofNullable();

    public WindowImpl(Widget rootWidget) {
        this.rootWidget = rootWidget;

        glassApp = Application.GetApplication();

        window = glassApp.createWindow(null,
                Window.TITLED | Window.CLOSABLE | Window.MAXIMIZABLE | Window.MINIMIZABLE);
        window.setEventHandler(new WindowEventHandlerImpl());
        window.setSize(innerSize.get().width, innerSize.get().height);
        window.setResizable(true);
        //window.setAlpha(0.5f);

        view = glassApp.createView();
        view.setEventHandler(new ViewEventHandlerImpl(this));
        //renderer = new PrismRenderer(view);
        window.setView(view);

        rootSurface = new GlassSurface(view, this);

        Renderer<?> renderer = Renderer.create(rootSurface);
        paintThread = new PaintThread<>(renderer, scheduler);
        paintThread.start();
        rootContentRequest = new Item.ItemRequest(rootSurface);

        Widget rootComponent = new Widget() {

            @Override
            protected Widget build() {
                Widget w = rootWidget;

                w = new Provider<>(TextStyle.class, DEFAULT_TEXT_STYLE, w);
                w = new Provider<>(Renderer.class, renderer, w);
                w = new Provider<>(Scheduler.class, scheduler, w);

                return ExposeRequest.requestSingle(w, rootContentRequest, result -> {
                    rootLayer.set(renderer.createLayer(rootLayer.get(), rootSurface.clipShape()));
                    rootLayer.get().setContent(result);
                    repaint();

                    return new SubstitutedWidget() {
                    };
                });
            }
        };

        try {
            scheduler.runAndWait(() -> {
                WidgetTree.create(rootComponent, this::submitTask);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException("Can't initialize widget tree: " + e.getCause(), e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for widget tree initialization", e);
        }

        window.setVisible(true);

        compositionTimingThread = new CompositorTimingThread(scheduler);
        compositionTimingThread.start();
    }

    public void repaint() {
        List<Frame.RenderDoneCallback> callbacks = List.copyOf(executeNextPaintTaskOnPlatformThread);
        executeNextPaintTaskOnPlatformThread.clear();
        scheduler.submitFrame(new Frame(rootLayer.get(), callbacks, rootSurface.width(), rootSurface.height()));
    }

    void submitTask(Runnable task) {
        scheduler.runLater(task);
    }

    /**
     * platform szálból van hívva
     */
    void onResize(ViewSize viewSize, Frame.RenderDoneCallback resizePaintCallback) {
        if (paintThread == null) {
            // még csak most nyitódik az ablak
            resizePaintCallback.willNotRender();
            submitTask(() -> {
                innerSize.set(viewSize);
            });
            return;
        }
        submitTask(() -> {
            executeNextPaintTaskOnPlatformThread.add(resizePaintCallback);
            innerSize.set(viewSize);
        });
    }

    record ViewSize(int width, int height) {
    }
}
