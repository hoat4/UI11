package ui11.platform.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.Component;
import ui11.RootElement;
import ui11.Slot;
import ui11.Widget;
import ui11.animation.Scheduler;
import ui11.control.*;
import ui11.css.CSSClassTag;
import ui11.css.WrapWithCSSClassTag;
import ui11.decoration.Box;
import ui11.document.TemplatedSVG;
import ui11.document.URLImageView;
import ui11.geom.Location.CoordinateSpaceRoot;
import ui11.graphics.Empty;
import ui11.graphics.effect.Mask;
import ui11.graphics.effect.Opacity;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.RoundedCorners;
import ui11.graphics.fill.*;
import ui11.input.focus.FocusListener;
import ui11.input.gesture.ClickListener;
import ui11.input.gesture.CloseRequestListener;
import ui11.input.pointer.PointerRegion;
import ui11.input.pointer.PointerTransparent;
import ui11.input.pointer.WithCursor;
import ui11.layout.multichild.Grid;
import ui11.layout.multichild.LinearLayout;
import ui11.layout.multichild.flow.Flow;
import ui11.layout.singlechild.*;
import ui11.media.Video;
import ui11.observable.InvalidationPoint;
import ui11.observable.Scope;
import ui11.platform.dom.peers.*;
import ui11.platform.dom.peers.DOMCoverPeer.CSSBackgroundImageContext;
import ui11.provide.Provide;
import ui11.provide.Provider;
import ui11.provide.UpValueWrapper;
import ui11.resolution.WidgetResolver;
import ui11.text.Text;
import ui11.text.TextAlign;
import ui11.text.TextStyle;
import ui11.text.TextStyle.FontWeight;
import ui11.text.formatted.OrderedList;
import ui11.webcontent.WebContentFrame;
import ui11.window.FileChooserProvider;
import ui11.window.Shell;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.Executor;

import static ui11.css.CSSClassTag.cssClass;
import static ui11.graphics.effect.Overlay.overlay;

public class DOMEnvironment implements WidgetResolver, Shell, Scheduler {

    // TODO hogy lehessen "normal" letter-spacingre visszaállítani?

    private static final Logger logger = LoggerFactory.getLogger(DOMEnvironment.class);

    private static final TextStyle DEFAULT_TEXT_STYLE = new TextStyle(Color.BLACK, 12D, "sans-serif",
            TextAlign.LEFT, FontWeight.NORMAL, true, false, null, null, null);

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

        class DOMElementRoot extends Component {

            @Inject private Slot rootWidgetSlot;

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
            WidgetResolver widgetResolver() {
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
            protected void update() {
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

                Widget contentRoot = new RootWidgetWrapper(widget);
                DOMElementHolder childElement =
                        rootWidgetSlot.instantiate(new DOMWidgetWrapper(contentRoot)).
                                lookup(DOMElementHolder.class);
                element.setInnerHTML("");
                element.appendChild(childElement.element());
            }
        }
        element.getClassList().add("fp");

        Executor executor = task -> window.setTimeout(task::run, 0);
        new RootElement(new DOMElementRoot(), executor).start();
    }

    @Nullable
    @Override
    public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
        // TODO ez valszeg eléggé lassú, hogy folyton lekérdezzük ezt, miközben Cover nagyon ritkán használt feature
        if (resolutionContext.optionalInherited(CSSBackgroundImageContext.class).isPresent()) {
            return switch (widget) {
                case URLImageView e -> {
                    if (e.interactive())
                        throw new RuntimeException("interactive URLImageView inside CSSBackgroundImageContext");
                    yield new UpValueWrapper(new DOMCoverPeer.CSSBackgroundImage(e.url()));
                }
                default -> null;
            };
        }

        return switch (widget) {
            case ColorFill cf -> new ColorFillPeer(cf);
            case Text s -> new TextElementPeer(s);
            case RasterImageView iv -> {
                throw new RuntimeException("TODO");
            }
            case LinearGradient g -> new DOMLinearGradientPeer(g);
            case ConicGradient g -> new DOMConicGradientPeer(g);
            case Mask m -> new DOMMaskPeer(m);
            case Opacity m -> new DOMOpacityPeer(m);
            case RoundedCorners r -> new DOMRoundedCornersPeer(r);
            case WebContentFrame wcf -> new WebContentFramePeer(wcf);
            case Empty e -> new EmptyElementPeer();
            case DOMElementWidget e -> new DOMElementWrapperPeer(e);
            case URLImageView e -> new URLImageViewPeer(e);
            case HTMLElementHint h -> new DOMWrapperElementPeer(h);
            case Hyperlink l -> new DOMHyperlinkPeer(l);
            case Video video -> new DOMVideoPeer(video);

            // ez hova tartozik?
            case TemplatedSVG svg -> new DOMTemplatedSVGPeer(svg);

            // INPUT
            case ClickListener c -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofOnClick(c.handler()), c.content());
            case FocusListener f -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofFocus(f), f.content());
            case PointerRegion r -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofPointerRegion(r), r.content());
            case PointerTransparent pt -> cssClass("Pt", pt.content());
            case CloseRequestListener closeRequestListener ->
                    new DOMCloseRequestListenerPeer(closeRequestListener);
            case WithCursor c -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofCursor(c.cursor()), c.content());

            // LAYOUT
            case Align a -> new DOMAlignPeer(a);
            case Box b -> new DOMBoxPeer(b);
            case Padding b -> new DOMPaddingPeer(b);
            case Grid g -> new DOMGridPeer(g);
            case LinearLayout l -> new DOMLinearLayoutPeer(l);
            case Overlay o -> new DOMOverlayLayoutPeer(o);
            case Flow f -> new DOMFlowLayoutPeer(f);
            case PassiveSize p -> new DOMPassiveSizePeer(p);
            case CSSClassTag c -> new Provider<>(CumulatingPropList.class,
                    CumulatingPropList.ofCSSClass(c.className()), c.content());
            case WrapWithCSSClassTag w -> cssClass(w.className(), overlay(w.content()));
            case Scrollable s -> new DOMScrollablePeer(s);
            case Hidden h -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofHidden(), h.content());
            case Cover c -> new DOMCoverPeer(c);
            case PassiveHeight p -> new DOMPassiveHeightPeer(p);
            // TODO ha itt önmagát adjuk vissza, azt detektálni kéne. most csak végtelen ciklusba kerülünk tőle.

            // FORMATTED TEXT
            case OrderedList ol -> new DOMOrderedListPeer(ol);

            // CONTROL
            case PlainTextEditor et -> new DOMEditableTextPeer(et);
            //case Button b -> new WidgetStateRequest<>(() -> new ButtonPeer(b, this), cf);
            case CheckBox cb -> new DOMCheckBoxPeer(cb);
            case RadioButton<?> rb -> new DOMRadioButtonPeer<>(rb);
            case ComboBox<?> cb -> new ComboBoxPeer<>(cb);
            case Slider slider -> new SliderPeer(slider);
            // TODO case StylesheetRef sr -> handleStylesheet(sr), sr);
            case Tooltip t -> new Provider<>(CumulatingPropList.class, CumulatingPropList.ofTooltipTag(t), t.content());

            default -> null;
        };
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
