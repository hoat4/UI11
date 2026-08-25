package ui11.layout.protocol;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequest;
import ui11.SubstitutedWidget;
import ui11.geom.Size;

import java.util.Map;
import java.util.Objects;

public sealed abstract class BoxLayoutResult extends SubstitutedWidget {

    public static final class OfChosenSize extends BoxLayoutResult {

        public final BoxConstraints inputConstraints;
        public final Size size;

        public OfChosenSize(@NonNull BoxConstraints inputConstraints, @NonNull Size size) {
            this.inputConstraints = Objects.requireNonNull(inputConstraints);
            this.size = Objects.requireNonNull(size);
        }
    }

    public static final class OfGone extends BoxLayoutResult {

        public static final OfGone INSTANCE = new OfGone();

        private OfGone() {
        }
    }

    public static class SizeRequest extends PeerRequest<BoxLayoutResult> {

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
