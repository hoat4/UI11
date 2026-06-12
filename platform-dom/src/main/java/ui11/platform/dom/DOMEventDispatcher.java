package ui11.platform.dom;

import ui11.geom.Vec2;
import ui11.observable.MutableObservable;
import ui11.observable.ObservableSet;
import ui11.geom.Location;
import ui11.input.pointer.Pointer;
import ui11.input.pointer.Pointer.MouseCursor;
import ui11.input.pointer.Pointer.StandardMouseButton;
import ui11.input.pointer.PointerRegion;
import ui11.platform.dom.bindings.JSTouch;
import ui11.platform.dom.bindings.TouchEvent;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.EventTarget;
import org.teavm.jso.dom.events.MouseEvent;
import org.teavm.jso.dom.events.Registration;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.xml.Node;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import java.util.*;
import java.util.function.Predicate;

// próbáltam az elemekre is rakni az eventlistenereket, de nem nagyon sikerült követni hogy pl. mikor engedik fel az
// egeret (mert lehet hogy másik node-ra került az egér, vagy átmozgatták azt a node-ot amin volt).
// Pointer Events API-nál már van egy setPointerCapture függvény, az jó lenne, de az csak új böngészőkben van.

// TODO Chromeban Windowson ha aktív touch közben átváltunk másik ablakra, és utána szüntetjük
//      meg a touchot, akkor azt a böngésző nem veszi észre, és új touch kezdésekor is
//      ott van a régi, nem létező a touches listában

// TODO mi történik, ha induláskor már be van nyomja az egér/touchscreen?

// TODO stylus support

class DOMEventDispatcher {

    private static final boolean TRACE = false;

    private final DOMEnvironment env;
    private final MouseImpl mouse = new MouseImpl();
    private final List<Registration> eventListenerRegistrations = new ArrayList<>();
    private final Map<Integer, TouchImpl> touches = new HashMap<>();

    public DOMEventDispatcher(DOMEnvironment env) {
        this.env = env;
    }

    void start() {
        env.window.getWindow().onEvent("mousedown", (EventListener<MouseEvent>) this::onMouseDown);
        env.window.getWindow().onEvent("mousemove", (EventListener<MouseEvent>) this::onMouseMove);
        env.window.getWindow().onEvent("mouseup", (EventListener<MouseEvent>) this::onMouseUp);
        env.window.getWindow().onEvent("mouseout", (EventListener<MouseEvent>) this::onMouseOut);

        // TODO valszeg vissza kéne térni arra hogy csak azokra az elemekre rakjuk, amelyiknél
        //      tényleg kellenek az eventek. csak akkor valahogy capture-ölni kéne.
        //      bár lehet úgy is, hogy a touchstart van Node szinten,
        //      az felrak windowra vagy documentre a touchmove/touchend/touchcancel-t.
        env.window.addNonPassiveEventListener(env.window.getWindow(),
                "touchstart", (EventListener<TouchEvent>) this::onTouchStart);
        env.window.addNonPassiveEventListener(env.window.getWindow(),
                "touchmove", (EventListener<TouchEvent>) this::onTouchMove);
        env.window.addNonPassiveEventListener(env.window.getWindow(),
                "touchend", (EventListener<TouchEvent>) this::onTouchEnd);
        env.window.addNonPassiveEventListener(env.window.getWindow(),
                "touchcancel", (EventListener<TouchEvent>) this::onTouchCancel);
    }

    void stop() {
        eventListenerRegistrations.forEach(Registration::dispose);
        eventListenerRegistrations.clear();
    }

    private void onMouseDown(MouseEvent mouseEvent) {
        // System.out.println("onMouseDown: " + mouseEvent);
        mouse.down(pointOfMouseEvent(mouseEvent), mouseEvent.getTarget(),
                mouseButton(mouseEvent), mouseButtons(mouseEvent));
        if (mouse.shouldPreventDefault())
            mouseEvent.preventDefault();
    }

    private void onMouseMove(MouseEvent mouseEvent) {
        if (TRACE)
            System.out.println("onMouseMove: " + mouseEvent + ", " + mouseEvent.getTarget());
        mouse.move(pointOfMouseEvent(mouseEvent), mouseEvent.getTarget(),
                mouseButtons(mouseEvent));
        if (mouse.shouldPreventDefault())
            mouseEvent.preventDefault();
    }

    private void onMouseUp(MouseEvent mouseEvent) {
        // System.out.println("onMouseUp: " + mouseEvent);
        mouse.up(pointOfMouseEvent(mouseEvent), mouseEvent.getTarget(),
                mouseButton(mouseEvent), mouseButtons(mouseEvent));
    }

    private void onMouseOut(MouseEvent mouseEvent) {
        if (TRACE)
            System.out.println("onMouseOut: " + mouseEvent + ", " + mouseEvent.getTarget());
        mouse.out(pointOfMouseEvent(mouseEvent), mouseEvent.getTarget(), mouseButtons(mouseEvent));
    }

    private void onTouchStart(TouchEvent evt) {
        System.out.println("onTouchStart: " + evt);
        for (int i = 0; i < evt.getChangedTouches().getLength(); i++) {
            JSTouch touch = evt.getChangedTouches().item(i);

            TouchImpl prevTouch = touches.get(touch.getIdentifier());
            if (prevTouch != null) {
                // ilyen elvileg nem fordulhat elő
                System.out.println("Touch #" + touch.getIdentifier() + " already exists, cancelling it");
                prevTouch.finish();
            }

            TouchImpl touch2 = new TouchImpl();
            touches.put(touch.getIdentifier(), touch2);
            touch2.down(pointOfTouch(touch), evt.getTarget());
        }
    }

    private void onTouchMove(TouchEvent evt) {
        for (int i = 0; i < evt.getChangedTouches().getLength(); i++) {
            JSTouch touch = evt.getChangedTouches().item(i);
            TouchImpl t = touches.get(touch.getIdentifier());
            if (t == null) {
                System.out.println("Ignoring touch move because " +
                        "touch #" + touch.getIdentifier() + " is not started");
                continue;
            }

            // TODO nézzünk utána, hogy lehet-e valami probléma abból, hogy ez nem Nodeot ad vissza,
            //      hanem HTMLElementet
            HTMLElement element = env.window.elementFromPoint(touch.getClientX(), touch.getClientY());
            // TouchEvent.target ilyenkor a lenyomáskori targetet tartalmazza
            t.move(pointOfTouch(touch), element);
        }
    }

    private void onTouchEnd(TouchEvent evt) {
        onTouchEndImpl(evt, false);
    }

    private void onTouchCancel(TouchEvent evt) {
        onTouchEndImpl(evt, true);
    }

    private void onTouchEndImpl(TouchEvent evt, boolean cancel) {
        for (int i = 0; i < evt.getChangedTouches().getLength(); i++) {
            JSTouch touch = evt.getChangedTouches().item(i);
            TouchImpl t = touches.remove(touch.getIdentifier());
            if (t == null) {
                System.out.println("Ignoring touch " + (cancel ? "cancel" : "end") + " because " +
                        "touch #" + touch.getIdentifier() + " is not started");
                continue;
            }

            // TODO locationnel csináljunk valamit?

            // ld. kommentek onTouchMove-ban
            HTMLElement element = env.window.elementFromPoint(touch.getClientX(), touch.getClientY());

            t.up(pointOfTouch(touch), element, cancel);
        }
    }

    private void dispatchPointerEvent(EventTarget target,
                                      Predicate<PointerRegion> h) {
        if (!(target instanceof Node n)) {
            System.out.println("event target is not DOM node");
            return;
        }

        for (; n != null; n = n.getParentNode()) {
            Object o = env.window.getData(n);
            if (o instanceof DOMSurface s) {
                for (PointerRegion r : s.pointerListeners) {
                    try {
                        if (h.test(r))
                            return;
                    } catch (Error | RuntimeException e) {
                        env.onUncaughtExceptionInEventHandler(e, "pointer event", o);
                    }
                }
            }
        }
    }

    private static StandardMouseButton mouseButton(MouseEvent evt) {
        return switch (evt.getButton()) {
            case 0 -> StandardMouseButton.PRIMARY;
            case 1 -> StandardMouseButton.MIDDLE;
            case 2 -> StandardMouseButton.SECONDARY;
            case 3 -> StandardMouseButton.BACK;
            case 4 -> StandardMouseButton.FORWARD;
            default -> null; // TODO logozni kéne, ha egyéb gombot találtunk
        };
    }

    @Nullable
    private Set<StandardMouseButton> mouseButtons(MouseEvent evt) {
        if (!env.window.hasButtonsProperty(evt))
            return null;
        Set<StandardMouseButton> buttons = EnumSet.noneOf(StandardMouseButton.class);
        if ((evt.getButtons() & 1) != 0)
            buttons.add(StandardMouseButton.PRIMARY);
        if ((evt.getButtons() & 2) != 0)
            buttons.add(StandardMouseButton.SECONDARY);
        if ((evt.getButtons() & 4) != 0)
            buttons.add(StandardMouseButton.MIDDLE);
        if ((evt.getButtons() & 8) != 0)
            buttons.add(StandardMouseButton.BACK);
        if ((evt.getButtons() & 16) != 0)
            buttons.add(StandardMouseButton.FORWARD);
        // TODO logozni kéne, ha egyéb gombot találtunk
        return buttons;
    }

    private @NonNull Location locationOfMouseEvent(MouseEvent evt) {
        return new Location(env.clientCoordinateSpace.origin, pointOfMouseEvent(evt));
    }

    private static @NonNull Vec2 pointOfMouseEvent(MouseEvent evt) {
        return new Vec2(evt.getClientX(), evt.getClientY());
    }

    private static @NonNull Vec2 pointOfTouch(JSTouch evt) {
        return new Vec2(evt.getClientX(), evt.getClientY());
    }

    private @NonNull Location locationOfTouch(JSTouch touch) {
        return new Location(env.clientCoordinateSpace.origin, new Vec2(touch.getClientX(), touch.getClientY()));
    }


    // TODO ez most nincs:
    //      ha egér le van nyomva, benyomom az Alt+Tabot, és ablakon kívül felengedem az egeret,
    //      akkor elveszik az a mouseup.
    //      ilyenkor a következő mousemove-kor vagy mousedown-kor korrigáljuk ezt
    //      MouseEvent.buttons alapján, de az nem minden böngészőben van.
    //      próbáltam PointerEvent-ekkel is, de azokkal se sikerült jobban.

    private abstract class DOMPointer implements Pointer {

        final MutableObservable<Location> location = MutableObservable.ofNullable();
        final Set<StandardMouseButton> pressedButtons = new ObservableSet<>();

        PointerRegion.PointerListener capture;
        private EventTarget targetAtLastMove;
        private Location locationAtCapture;

        @Override
        public Location location() {
            return location.get();
        }

        @Override
        public Set<StandardMouseButton> pressedButtons() {
            return Set.copyOf(pressedButtons);
        }

        boolean shouldPreventDefault() {
            return capture != null;
        }

        void move(Vec2 point, EventTarget target, Set<StandardMouseButton> allPressedButtons) {
            corrigateReleasedButtons(allPressedButtons);
            corrigatePos(point, target, false);
            corrigatePressedButtons(allPressedButtons);
        }

        void out(Vec2 point, EventTarget target, Set<StandardMouseButton> allPressedButtons) {
            corrigateReleasedButtons(allPressedButtons);
            corrigatePos(point, target, true);
            corrigatePressedButtons(allPressedButtons);
        }

        void corrigatePos(Vec2 point, EventTarget target, boolean out) {
            // TODO ha a target megváltozik, de a point nem, akkor mit csináljunk?
            this.targetAtLastMove = target;

            Location loc = new Location(env.clientCoordinateSpace.origin, point);
            boolean changed = !loc.equals(this.location.get());
            if (!out && !changed)
                return;

            this.location.set(loc);
            if (capture != null) {
                if (changed) {
                    if (TRACE)
                        System.out.println("dispatch move to " + env.window.getData(target));
                    capture.onMove(); // TODO exceptionök
                }
                // TODO el kéne tárolni az outokat és a capture végén fireölni
            } else {
                if (TRACE)
                    System.out.println("dispatch move (out=" + out + ") to " + env.window.getData(target));
                dispatchPointerEvent(target, r -> {
                    r.onPointerMove(this, !out);
                    return false;
                });
            }
        }

        void down(Vec2 point, EventTarget target,
                  StandardMouseButton button,
                  Set<StandardMouseButton> allPressedButtons) {
            corrigateReleasedButtons(allPressedButtons);
            corrigatePos(point, target, false);
            if (this.pressedButtons.add(button))
                downImpl(button);
            corrigatePressedButtons(allPressedButtons);
        }

        private void downImpl(StandardMouseButton button) {
            if (capture != null)
                capture.onPress(button); // TODO exceptionök
            else
                dispatchPointerEvent(targetAtLastMove, r -> {
                    capture = r.onPointerDown(this, button);
                    locationAtCapture = location.get();
                    return capture != null;
                });
        }

        void up(Vec2 point, EventTarget target,
                StandardMouseButton button, Set<StandardMouseButton> allPressedButtons) {

            Set<StandardMouseButton> allPressedButtons2 = EnumSet.noneOf(StandardMouseButton.class);
            allPressedButtons2.addAll(allPressedButtons);
            allPressedButtons2.add(button);
            corrigateReleasedButtons(allPressedButtons2);

            corrigatePos(point, target, false);

            if (this.pressedButtons.remove(button))
                upImpl(button);

            corrigatePressedButtons(allPressedButtons);
        }

        private void corrigatePressedButtons(Set<StandardMouseButton> allPressedButtons) {
            if (allPressedButtons != null) {
                for (StandardMouseButton btn : allPressedButtons)
                    if (this.pressedButtons.add(btn))
                        downImpl(btn);
            }
        }

        private void corrigateReleasedButtons(Set<StandardMouseButton> allPressedButtons) {
            if (allPressedButtons != null)
                for (Iterator<StandardMouseButton> iterator = this.pressedButtons.iterator(); iterator.hasNext(); ) {
                    StandardMouseButton btn = iterator.next();
                    if (!allPressedButtons.contains(btn)) {
                        iterator.remove();
                        upImpl(btn);
                    }
                }
        }

        private void upImpl(StandardMouseButton btn) {
            if (capture != null) {
                capture.onRelease(btn); // TODO exceptionök
                if (this.pressedButtons.isEmpty()) {
                    capture.onFinish();
                    capture = null;

                    Location locationAtCapture_ = locationAtCapture;
                    locationAtCapture = null;
                    if (!Objects.equals(location.get(), locationAtCapture_)) {
                        dispatchPointerEvent(targetAtLastMove, r -> {
                            r.onPointerMove(this, true);
                            return false;
                        });
                    }
                }
            }
        }

        /**
         * ezután semelyik másik függvényét nem szabad meghívnunk ennek a Pointernek
         */
        void finish() {
            if (capture != null) {
                capture.onFinish(); // TODO exceptionök
                capture = null;
            }
        }
    }

    private class MouseImpl extends DOMPointer implements MouseCursor {
    }

    private class TouchImpl extends DOMPointer implements MouseCursor {

        void down(Vec2 point, EventTarget target) {
            // így hasonlít PointerEvent spechez. de lehet hogy inkább be kéne vezetni
            // inkább egy új Pointer.Button altípust.

            down(point, target, StandardMouseButton.PRIMARY, Set.of(StandardMouseButton.PRIMARY));
        }

        void move(Vec2 point, EventTarget target) {
            move(point, target, Set.of(StandardMouseButton.PRIMARY));
        }

        void up(Vec2 point, EventTarget target, boolean cancel) {
            if (cancel)
                corrigatePos(point, target, false);
            else
                up(point, target, StandardMouseButton.PRIMARY, Set.of());
            finish();
        }
    }
}
