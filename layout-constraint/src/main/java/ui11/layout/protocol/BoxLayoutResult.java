package ui11.layout.protocol;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.Widget;
import ui11.geom.Size;
import ui11.ParentDataWidget;
import ui11.PeerCreationRequest;
import ui11.layout.multichild.LinearLayout;

import java.util.Objects;

public sealed abstract class BoxLayoutResult extends ParentDataWidget {

    private BoxLayoutResult(Widget next) {
        super(next);
    }

    // TODO ez kb. ugyanaz mint FixedSize. ezzel majd kéne kezdeni valamit, pl. lehet hogy ezt valahogy meg lehetne
    //      szüntetni, de még nem tudom, hogy hogyan
    public static final class OfChosenSize extends BoxLayoutResult {

        private final @NonNull Size size;

        public OfChosenSize(@NonNull Size size, Widget content) {
            super(content);
            this.size = Objects.requireNonNull(size);
        }

        public @NonNull Size size() {
            return size;
        }
    }

    public static final class OfNoConstraints extends BoxLayoutResult {
        public OfNoConstraints(Widget content) {
            super(content);
        }
    }

    public static final class OfGone extends BoxLayoutResult {
        public OfGone(Widget content) {
            super(content);
        }
    }

    public static class SizeRequest extends PeerCreationRequest<BoxLayoutResult> {

        private final BoxConstraints constraints;

        /**
         *
         * @param constraints ez null, ha még csak az érdekel hogy gone-e
         */
        public SizeRequest(@Nullable BoxConstraints constraints) {
            super(BoxLayoutResult.class, LinearLayout.WeightMarker.class);
            this.constraints = constraints;
        }

        public BoxConstraints constraints() {
            return constraints;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            SizeRequest that = (SizeRequest) o;
            return Objects.equals(constraints, that.constraints);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(constraints);
        }

        @Override
        public String toString() {
            return "SizeRequest[constraints=" + constraints + "]";
        }
    }
}
