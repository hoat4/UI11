package ui11.platform.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.PeerRequest;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.WidgetTree;
import ui11.animation.Scheduler;
import ui11.color.Color;
import ui11.geom.Location.CoordinateSpaceRoot;
import ui11.observable.InvalidationPoint;
import ui11.observable.Scope;
import ui11.provide.Provide;
import ui11.text.TextAlign;
import ui11.text.TextStyle;
import ui11.text.TextStyle.FontWeight;
import ui11.window.FileChooserProvider;
import ui11.window.Shell;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

public class DOMEnvironment implements Shell, Scheduler {

    // TODO hogy lehessen "normal" letter-spacingre visszaállítani?

    private static final Logger logger = LoggerFactory.getLogger(DOMEnvironment.class);

    // TODO default size 13-nak van beállítva common.css-ben, de nem erre kéne hagyatkozni
    private static final TextStyle DEFAULT_TEXT_STYLE = new TextStyle(Color.BLACK, 13D, "sans-serif",
            TextAlign.LEFT, FontWeight.NORMAL, TextStyle.Wrapping.BETWEEN_WORDS,
            false, null, null, null);

    public final JSWindowWrapper window;
    public final HTMLDocument document;
    private final DOMEventDispatcher eventDispatcher = new DOMEventDispatcher(this);
    public final CoordinateSpaceRoot clientCoordinateSpace = new CoordinateSpaceRoot();

    public DOMEnvironment(Window window) {
        this.window = JSWindowWrapper.ofNative(window);
        this.document = window.getDocument();
    }

    public static void show(Widget widget, HTMLElement element) {
        new DOMEnvironment(Window.current()).doShow(widget, element);
    }

    public void doShow(Widget widget, HTMLElement element) {
        Objects.requireNonNull(element, "The specified HTML container element is null");

        // TODO csak egyszer kéne hozzáadnunk a documenthez
        for (GlobalCSSSnippetProvider p : ServiceLoader.load(GlobalCSSSnippetProvider.class)) {
            HTMLElement css = document.createElement("style");
            css.setTextContent(p.cssSnippet());
            document.getHead().appendChild(css);
        }

        class DOMElementRoot extends Widget {


            @Override
            protected void onResume() {
                // TODO ez is lehet hogy inkább az update után kéne
                eventDispatcher.start();
                untilPause().onClose(eventDispatcher::stop);
            }

            @Provide
            DOMEnvironment domEnv() {
                // mi nem használjuk, de más modul lehet hogy igen (pl. lottie-web)
                return DOMEnvironment.this;
            }

            @Provide
            TextStyle defaultTextStyle() {
                return DEFAULT_TEXT_STYLE;
            }

            @Provide
            FileChooserProvider fileChooserProvider() {
                return new DOMFileChooserProvider(window);
            }

            @Provide
            Shell shell() {
                return DOMEnvironment.this;
            }

            @Provide
            Scheduler scheduler() {
                return DOMEnvironment.this;
            }

            @Provide
            CumulatingPropList initialCumulatingPropList() {
                return CumulatingPropList.CLEAR;
            }

            @Override
            protected Widget build() {
                // ide berakni Provider-eket nem teljesen ugyanaz, mint a fenti @Provide metódusok:
                // ha itt exception keletkezik, akkor a @Provide-ot figyelembe lesznek véve az errorwidget
                // előállításakor, de az itt létrehozott Provider-ek viszont nyilván nem

                // ez azért kell, hogy ha nem sikerül w-t instantiate-elni, akkor ez az update() szálljon el,
                // mert akkor nem lesz kijelezve a delegate creation error.
                // TODO a default delegatecreationfailed szöveg nem jól látható, kis fekete betűk,
                //      bowling háttér tetejébe jól beolvad
                final class RootWidgetWrapper extends Widget {
                    private final Widget w;

                    RootWidgetWrapper(Widget w) {
                        this.w = w;
                    }

                    @Override
                    protected Widget build() {
                        return w;
                    }
                }

                Widget contentRoot = new RootWidgetWrapper(new DOMWidgetWrapper(widget));
                final DOMPeerBase.DOMPeerCreationRequest rootContentReq = DOMPeerBase.DOMPeerCreationRequest.INSTANCE;
                return PeerRequest.requestSingle(contentRoot, rootContentReq, result -> {
                    element.setInnerHTML("");
                    element.appendChild(result.element());
                    return new SubstitutedWidget() {
                    };
                });
            }
        }
        element.getClassList().add("fp");

        Executor executor = task -> window.setTimeout(task::run, 0);
        WidgetTree.create(new DOMElementRoot(), executor);
    }

    @Override
    public void openURL(URI target) {
        Window.current().open(target.toString(), "_blank");
    }

    @Override
    public void runLater(Runnable task) {
        Objects.requireNonNull(task);
        window.setTimeout(task::run, 0);
    }

    @Override
    public void requestAnimationFrame() {
        InvalidationPoint ip = new InvalidationPoint();
        ip.subscribe();
        Window.requestAnimationFrame(timestamp -> ip.invalidate());
    }

    @Override
    public void scheduleOneTime(Duration delay, Runnable task, Scope scope) {
        if (delay.isNegative())
            throw new IllegalArgumentException();
        Objects.requireNonNull(task);

        long remainingTimeMS;
        try {
            remainingTimeMS = delay.toMillis();
        } catch (ArithmeticException e) {
            // 292277025 év múlva vélhetően nem fogunk futni, feleslegesen setTimeoutot beállítani
            return;
        }
        double remainingTimeMSDouble = remainingTimeMS > 1_000_000_000 ? remainingTimeMS :
                delay.toNanos() / 1_000_000.0;

        int timeoutID = window.setTimeout(task::run, remainingTimeMSDouble);
        scope.onClose(() -> window.clearTimeout(timeoutID));
    }

    @Override
    public void scheduleAtFixedRate(Duration delay, Runnable task, Scope scope) {
        if (delay.isNegative()) // isZero?
            throw new IllegalArgumentException();
        Objects.requireNonNull(task);

        long remainingTimeMS;
        try {
            remainingTimeMS = delay.toMillis();
        } catch (ArithmeticException e) {
            // 292277025 év múlva vélhetően nem fogunk futni, ezért felesleges setIntervalt hívni
            return;
        }
        double remainingTimeMSDouble = remainingTimeMS > 1_000_000_000 ? remainingTimeMS :
                delay.toNanos() / 1_000_000.0;

        int intervalID = Window.setInterval(task::run, remainingTimeMSDouble);
        scope.onClose(() -> window.clearInterval(intervalID));
    }

    public <E extends Event> EventListener<E> wrapEventListener(EventListener<E> eventListener,
                                                                Object source) {
        return evt -> {
            try {
                eventListener.handleEvent(evt);
            } catch (Throwable e) {
                onUncaughtExceptionInEventHandler(e, evt, source);
            }
        };
    }

    public void onUncaughtExceptionInEventHandler(Throwable e, Object event, Object source) {
        logger.error("Event handler of " + source + " failed to process " + event, e);
    }
}
