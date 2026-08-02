package ui11.layout.protocol;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequestor;
import ui11.geom.Size;
import ui11.layout.multichild.LinearLayout;

import java.util.Objects;

public sealed interface BoxLayoutResult {

    record OfChosenSize(@NonNull Size size) implements BoxLayoutResult {

        public OfChosenSize {
            Objects.requireNonNull(size);
        }
    }

    record OfGone() implements BoxLayoutResult {
    }

    public static class SizeRequest extends PeerRequestor.Request<BoxLayoutResult> {

        private final BoxConstraints constraints;

        public SizeRequest(@NonNull BoxConstraints constraints) {
            super(BoxLayoutResult.class);
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
