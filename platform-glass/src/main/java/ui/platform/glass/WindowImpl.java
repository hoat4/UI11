package ui.platform.glass;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.View;
import com.sun.glass.ui.Window;
import ui.platform.glass.windows.CompositorTimingThread;
import ui11.*;
import ui11.animation.Scheduler;
import ui11.color.Color;
import ui11.geom.Mat4;
import ui11.geom.Vec2;
import ui11.graphics.Surface;
import ui11.observable.MutableObservable;
import ui11.platform.opengl.BufferPool;
import ui11.platform.opengl.GLNodeHolder;
import ui11.platform.opengl.GLSurface;
import ui11.platform.opengl.GLViewProvider;
import ui11.platform.opengl.renderer.displaylist.DisplayList;
import ui11.provide.Provider;
import ui11.WidgetResolver;
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
    public static final GLViewProvider DEFAULT_VIEW_PROVIDER = GLViewProvider.INSTANCE;

    public final Application glassApp;
    private final Window window;
    private final Widget rootWidget;
    private View view;

    private final SchedulerImpl scheduler = new SchedulerImpl();
    final PaintThread paintThread;

    /**
     * UI szálból írjuk és olvassuk
     */
    private final List<DisplayList.RenderDoneCallback> executeNextPaintTaskOnPlatformThread = new ArrayList<>();

    DisplayList currentDisplayList;

    final MutableObservable<ViewSize> innerSize = MutableObservable.withInitial(new ViewSize(300, 300));
    private final GLSurface.RootGLSurface rootSurface;

    private final CompositorTimingThread compositionTimingThread;

    public WindowImpl(Widget rootWidget) {
        this.rootWidget = rootWidget;

        glassApp = Application.GetApplication();
        rootSurface = new GLSurface.RootGLSurface(innerSize.map(vs -> new Vec2(vs.width, vs.height)));

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

        paintThread = new PaintThread(view, scheduler);
        paintThread.start();

        BufferPool bufferPool = new BufferPool();

        Widget rootComponent = new Widget() {

            @Override
            protected Widget build() {
                Widget w = rootWidget;

                w = new Provider<>(TextStyle.class, DEFAULT_TEXT_STYLE, w);
                w = new Provider<>(WidgetResolver.class, DEFAULT_VIEW_PROVIDER, w);
                w = new Provider<>(BufferPool.class, bufferPool, w);
                w = new Provider<>(Scheduler.class, scheduler, w);

                DisplayList displayList = new DisplayList(innerSize.get().width, innerSize.get().height);

                Mat4 initialTransform = new Mat4(
                        2.0 / displayList.viewportWidth, 0, 0, -1,
                        0, -2.0 / displayList.viewportHeight, 0, 1,
                        0, 0, 1, 0,
                        0, 0, 0, 1
                );


                return PeerRequestor.ofSingle(w, rootSurface, result -> {

                /*
                System.out.println("New render tree. Viewport size: "+innerSize.get());
                System.out.println(RenderNode.RenderTreePrinter.toString(rootRenderNode));
                System.out.println();
                 */

                    result.peer().renderNode().addToDisplayList(initialTransform, displayList);
                    currentDisplayList = displayList;
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
        if (currentDisplayList == null)
            return;

        currentDisplayList.renderDoneCallbacks.addAll(executeNextPaintTaskOnPlatformThread);
        executeNextPaintTaskOnPlatformThread.clear();
        scheduler.submitFrame(currentDisplayList);
    }

    void submitTask(Runnable task) {
        scheduler.runLater(task);
    }

    /**
     * platform szálból van hívva
     */
    void onResize(ViewSize viewSize, DisplayList.RenderDoneCallback resizePaintCallback) {
        if (paintThread.renderer == null) {
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
