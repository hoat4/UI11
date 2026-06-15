package ui11.layout.protocol;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.EndingWidget;
import ui11.geom.Size;
import ui11.resolution.PeerCreationRequest;

import java.util.Objects;

public sealed abstract class BoxLayoutResult extends EndingWidget {

    public static final class OfChosenSize extends BoxLayoutResult {

        private final @NonNull Size size;

        public OfChosenSize(@NonNull Size size) {
            this.size = Objects.requireNonNull(size);
        }

        public @NonNull Size size() {
            return size;
        }
    }

    public static final class OfNoConstraints extends BoxLayoutResult {
    }

    public static final class OfGone extends BoxLayoutResult {
    }

    public static class SizeRequest extends PeerCreationRequest<BoxLayoutResult> {

        private final BoxConstraints constraints;

        /**
         *
         * @param constraints ez null, ha még csak az érdekel hogy gone-e
         */
        public SizeRequest(@Nullable BoxConstraints constraints) {
            super(BoxLayoutResult.class);
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
