package ui11.layout.helper;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.*;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.protocol.BoxLayoutResult;
import ui11.geom.Mat4;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.shaper.RectangleShaped;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.layout.protocol.BoxConstraints;
import ui11.provide.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO most az ide-oda rángatás miatt kétszer buildelődnek a childek

public class MultiChildLayout extends Widget {

    private static final Logger logger = LoggerFactory.getLogger(MultiChildLayout.class);

    private final MultiChildLayoutDelegate delegate;

    @Inject(required = false) private BoxLayoutResult.SizeRequest sizeRequest;
    @Inject(required = false) private Surface surface;
    @Inject private MultiSlot<Object> childSlots;
    @Inject private MultiSlot<Object> childTransformSlots;

    @Remember private List<Widget> elements; // TODO ez valójában nem @Remember, csak build közben van használva
    @Remember private MultiChildLayoutCallback currentCallback;

    public MultiChildLayout(MultiChildLayoutDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints = sizeRequest != null ? sizeRequest.constraints() : null;

        if (constraints == null) {
            if (surface == null)
                throw new IllegalStateException("no " + Surface.class.getSimpleName() + " or " +
                        BoxConstraints.class.getSimpleName() + " provided for " + this);
            constraints = BoxConstraints.tight(surface.size());
        }
        if (elements != null)
            throw new IllegalStateException();

        // TODO ha constraints megegyezik az előzővel, akkor csak vissza kéne adni az előző eredményt, nem újra
        //      meghívni a doLayoutot. de az a baj, hogy lehet hogy a doLayoutnak változott meg egy observable-je.

        elements = new ArrayList<>();
        Widget result;
        Size s;
        try {
            currentCallback = new MultiChildLayoutCallback();
            try {
                s = delegate.doLayout(constraints, currentCallback);
            } finally {
                currentCallback = null;
            }

            if (!constraints.isSatisfiedBy(s)) {
                logger.error(constraints + " is not satisfied by " + s + " (returned by " + delegate + ")");
                return new ColorFill(Color.CYAN);
            }

            result = new Overlay(elements);
        } finally {
            elements = null;
        }

        // hogy DefaultOverlayLayoutImpl ne kezdjen méretet számolni, mert egyrészt felesleges,
        // másrészt mert még nem is tud, mert Transform, Clip, stb. nem forwardolja a BoxLayoutProtocolt
        result = new Provider<>(BoxConstraints.class, null, result);

        if (sizeRequest != null)
            result = EndingWidget.combine(result, new BoxLayoutResult.OfChosenSize(s));

        return result;
    }

    @Override
    public String toString() {
        if (childSlots == null) // TODO ez egy hack, csak azt nézzük hogy volt-e már inicializálva ez a widget
            return super.toString();
        return super.toString() + "{req=" + sizeRequest + ", " +
                "currentCallback " + (currentCallback == null ? "==" : "!=") + " null}";
    }

    public interface MultiChildLayoutDelegate {

        Size doLayout(BoxConstraints constraints, MultiChildLayoutCallback callback);
    }

    public final class MultiChildLayoutCallback {

        // TODO ezt az API-t le kéne egyszerűsíteni.
        //      viszont egyúttal ki is kéne bővíteni hogy supportálja az olyan layout property-ket,
        //      mint pl. LinearLayoutnál a weight (most csak első Elementig van megnézve).

        public void place(Object key, Widget w, Rect rect) {
            place(key, w, rect, null);
        }

        public void place(Object key, Widget w, Rect rect, BoxConstraints constraints) {
            if (currentCallback != this)
                throw new IllegalStateException("place() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            // azért kell két slot, mert különben a cachedPeer egyszer Transform lenne, egyszer meg a rendes widget
            elements.add(new Provider<>(BoxConstraints.class, constraints,
                    new Transform(
                            new RectangleShaped(
                                    w.withSlot(childSlots.get(key)),
                                    rect.size()
                            ),
                            Mat4.ofTranslation(rect.topLeft())
                    )
            ).withSlot(childTransformSlots.get(key)));
        }

        public void placeOverlay(Object key, Widget w) {
            placeOverlay(key, w, null);
        }

        public void placeOverlay(Object key, Widget w, BoxConstraints constraints) {
            if (currentCallback != this)
                throw new IllegalStateException("placeOverlay() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            elements.add(new Provider<>(BoxConstraints.class, constraints,
                    w.withSlot(childSlots.get(key))));
        }

        private Size measure(Object key, Widget widget, @NonNull BoxConstraints constraints) {
            if (currentCallback != this)
                throw new IllegalStateException("measure() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            BoxLayoutResult layoutResult = makePeer(childSlots.get(key), widget,
                    new BoxLayoutResult.SizeRequest(constraints));
            return switch (layoutResult) {
                case BoxLayoutResult.OfGone _ -> {
                    logger.error("Gone in measure: " + key + ", " + widget + ", " + constraints);
                    yield constraints.min();
                }
                case BoxLayoutResult.OfChosenSize r -> r.size();
                case BoxLayoutResult.OfNoConstraints _ -> {
                    throw new RuntimeException("null layout result: " + widget + ", " + constraints + ", " + layoutResult);
                }
            };
        }

        public Placeable asPlaceable(Object key, Widget widget) {
            Placeable p = asPlaceableOrNull(key, widget);
            return p == null ? new Placeable(null, null) : p;
        }

        public @Nullable Placeable asPlaceableOrNull(Object key, Widget widget) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(widget, "widget");

            if (currentCallback != this)
                throw new IllegalStateException("asPlaceableOrNull() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            // TODO ilyenkor még nem kéne megadni nekik Surface-t, mert rossz méret van benne
            BoxLayoutResult r = makePeer(childSlots.get(key), widget,
                    new BoxLayoutResult.SizeRequest(null));
            if (r instanceof BoxLayoutResult.OfGone)
                return null;

            return new Placeable(key, widget);
        }

        public final class Placeable {

            @Nullable
            private final Object key;
            @Nullable
            private final Widget w;

            private boolean placed;
            private BoxConstraints lastConstraints;

            private Placeable(@Nullable Object key, @Nullable Widget w) {
                this.key = key;
                this.w = w;
            }

            /**
             * ha be akarjuk valami dekorációba wrappelni
             */
            public Widget widget() {
                if (w == null)
                    throw new UnsupportedOperationException();
                if (currentCallback != MultiChildLayoutCallback.this)
                    throw new IllegalStateException("Placeable.widget() called while not in " +
                            MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" +
                            MultiChildLayoutCallback.this + ")");
                return w.withSlot(childSlots.get(key));
            }

            public void placeAsOverlay() {
                if (placed)
                    // főleg GONE miatt nézzük itt.
                    // TODO MCLCallback.place-ben és MCLCallback.placeOverlay-ben amúgy is kéne nézni.
                    throw new IllegalStateException();

                placed = true;
                if (w != null)
                    placeOverlay(key, w, lastConstraints);
            }

            public void placeAt(Rect rect) {
                if (placed)
                    // ld. komment feljebb
                    throw new IllegalStateException();
                placed = true;

                if (w != null)
                    place(key, w, rect, lastConstraints);
            }

            public Size measure(@NonNull BoxConstraints constraints) {
                Objects.requireNonNull(constraints);

                if (w == null)
                    return constraints.min();

                return MultiChildLayoutCallback.this.measure(key, w, lastConstraints = constraints);
            }

            @Nullable
            public BoxConstraints lastMeasureConstraints() {
                return lastConstraints;
            }
        }
    }
}
