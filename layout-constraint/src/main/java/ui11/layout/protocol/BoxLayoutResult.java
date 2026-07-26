package ui11.layout.protocol;

import org.jspecify.annotations.NonNull;
import ui11.ParentDataWidget;
import ui11.PeerCreationRequest;
import ui11.Widget;
import ui11.geom.Size;
import ui11.layout.multichild.LinearLayout;

import java.util.Objects;

public sealed abstract class BoxLayoutResult extends ParentDataWidget {

    private BoxLayoutResult(Widget next) {
        super(next);
    }

    public static final class OfChosenSize extends BoxLayoutResult {

        private final @NonNull Size size;
        private final BoxConstraints constraints;

        public OfChosenSize(@NonNull Size size, Widget content, @NonNull BoxConstraints constraints) {
            super(content);
            this.size = Objects.requireNonNull(size);
            this.constraints = Objects.requireNonNull(constraints);
        }

        public @NonNull Size size() {
            return size;
        }

        @Override
        protected boolean matches(@NonNull PeerCreationRequest<?> requestData) {
            return ((SizeRequest) requestData).constraints.equals(constraints);
        }
    }

    public static final class OfGone extends BoxLayoutResult {
        public OfGone(Widget content) {
            super(content);
        }
    }

    public static class SizeRequest extends PeerCreationRequest<BoxLayoutResult> {

        private final BoxConstraints constraints;

        public SizeRequest(@NonNull BoxConstraints constraints) {
            super(BoxLayoutResult.class, LinearLayout.WeightMarker.class);
            this.constraints = Objects.requireNonNull(constraints);
        }

        public @NonNull BoxConstraints constraints() {
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
