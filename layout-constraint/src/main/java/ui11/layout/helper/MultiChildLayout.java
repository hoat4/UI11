package ui11.layout.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.*;
import ui11.layout.Gone;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.geom.Mat4;
import ui11.geom.Rect;
import ui11.geom.Size;
import ui11.graphics.Surface;
import ui11.graphics.effect.ClipRect;
import ui11.graphics.effect.Overlay;
import ui11.graphics.effect.Transform;
import ui11.layout.protocol.BoxConstraints;
import ui11.layout.protocol.BoxLayoutProtocol;
import ui11.provide.Provider;
import ui11.provide.UpValueWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// TODO most az ide-oda rángatás miatt kétszer buildelődnek a childek

public class MultiChildLayout extends Widget implements BoxLayoutProtocol {

    private static final Logger logger = LoggerFactory.getLogger(MultiChildLayout.class);

    private final MultiChildLayoutDelegate delegate;

    @Inject(required = false) private Observable<BoxConstraints> constraints;
    @Inject(required = false) private Observable<Surface> surface;
    @Inject private MultiSlot<Object> childSlots;
    @Inject private MultiSlot<Object> childTransformSlots;

    @State private MutableObservable<Size> determinedSize; // TODO ezt nem kéne törölni valamikor?
    @State private List<Widget> elements; // TODO ez valójában nem @State, csak build közben van használva
    @State private MultiChildLayoutCallback currentCallback;

    public MultiChildLayout(MultiChildLayoutDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected void initState() {
        determinedSize = MutableObservable.ofNullable();
    }

    @Override
    protected Widget build() {
        BoxConstraints constraints = this.constraints.get();
        Surface surface = this.surface.get();

        boolean fromConstraints = constraints != null;
        if (constraints == null) {
            if (surface == null)
                throw new IllegalStateException("no " + Surface.class.getSimpleName() + " or " +
                        BoxConstraints.class.getSimpleName() + " provided for " + this);
            constraints = BoxConstraints.tight(surface.size());
        }
        if (elements != null)
            throw new IllegalStateException();

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

            if (!constraints.isSatisfiedBy(s))
                throw new RuntimeException(constraints + " is not satisfied by " + s + " (returned by " + this + ")");
            result = new Overlay(elements);
        } finally {
            elements = null;
        }

        if (fromConstraints)
            // ez meghívhat más kódot, ezért ne fent hívjuk, amíg buildContext nincs nullra visszaállítva
            determinedSize.set(s);

        // hogy DefaultOverlayLayoutImpl ne kezdjen méretet számolni, mert egyrészt felesleges,
        // másrészt mert még nem is tud, mert Transform, Clip, stb. nem forwardolja a BoxLayoutProtocolt
        result = new Provider<>(BoxConstraints.class, null, result);
        return new UpValueWrapper(this, result);
    }

    @Override
    public Size preferredSize(BoxConstraints constraints) {
        Size s = determinedSize.get();
        if (s == null)
            throw new IllegalStateException();
        return s;
    }

    @Override
    public String toString() {
        if (constraints == null)
            return super.toString();
        return super.toString() + "{constraints=" + constraints.snoop() + ", determinedSize=" + determinedSize.snoop() + ", " +
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
            elements.add(childTransformSlots.use(key,
                    new Provider<>(BoxConstraints.class, constraints,
                            new Transform(
                                    new ClipRect(childSlots.use(key, w), rect.size()),
                                    Mat4.ofTranslation(rect.topLeft())))));
        }

        public void placeOverlay(Object key, Widget w) {
            placeOverlay(key, w, null);
        }

        public void placeOverlay(Object key, Widget w, BoxConstraints constraints) {
            if (currentCallback != this)
                throw new IllegalStateException("placeOverlay() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            elements.add(new Provider<>(BoxConstraints.class, constraints,
                    childSlots.use(key, w)));
        }

        private Size measure(Object key, Widget widget, @Nonnull BoxConstraints constraints) {
            if (currentCallback != this)
                throw new IllegalStateException("measure() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            WidgetInstantiation h = childSlots.instantiate(key,
                    new Provider<>(BoxConstraints.class, constraints, widget));
            if (h.lookupOptional(Gone.class).isPresent()) {
                logger.error("Gone in measure: " + key + ", " + widget + ", " + constraints);
                return constraints.min();
            }
            BoxLayoutProtocol p = h.lookup(BoxLayoutProtocol.class);
            return p.preferredSize(constraints);
        }

        public Placeable asPlaceable(Object key, Widget widget) {
            Placeable p = asPlaceableOrNull(key, widget);
            return p == null ? new Placeable(null, null) : p;
        }

        @Nullable
        public Placeable asPlaceableOrNull(Object key, Widget widget) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(widget, "widget");

            if (currentCallback != this)
                throw new IllegalStateException("asPlaceableOrNull() called while not in " +
                        MultiChildLayoutDelegate.class.getSimpleName() + ".delegate (" + this + ")");

            WidgetInstantiation h = childSlots.instantiate(key, widget);
            if (h.lookupOptional(Gone.class).isPresent())
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
                return childSlots.use(key, w);
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

            public Size measure(@Nonnull BoxConstraints constraints) {
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
