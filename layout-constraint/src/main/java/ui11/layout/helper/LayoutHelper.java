package ui11.layout.helper;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequest;
import ui11.Widget;
import ui11.geom.Size;
import ui11.graphics.VisualContentRequest;
import ui11.layout.protocol.BoxLayoutResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LayoutHelper extends Widget {

    private final LayoutHelperDelegate delegate;

    // TODO keys missing from this
    @Inject private BoxLayoutResult.SizeRequest[] sizeRequests;
    @Inject(required = false) private VisualContentRequest<?> surface;

    public LayoutHelper(LayoutHelperDelegate delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    protected Widget build() {
        if (surface == null && sizeRequests.length == 0)
            throw new RuntimeException("No surface and no size requests at same time, nothing to return");

        List<Widget> w = new ArrayList<>();

        for (BoxLayoutResult.SizeRequest sizeRequest : sizeRequests)
            w.add(delegate.computePreferredSize(sizeRequest));

        return PeerRequest.requestMultiple(w, List.of(sizeRequests), sizes -> {
            Widget prev;
            if (surface == null)
                prev = null;
            else
                prev = Objects.requireNonNull(delegate.computeChildArrangement(surface.size()));

            for (int i = sizeRequests.length - 1; i >= 0; i--) {
                if (prev == null)
                    prev = sizeRequests[i].createResponse(sizes.get(i));
                else
                    prev = sizeRequests[i].createResponse(sizes.get(i), prev);
            }

            assert prev != null;

            return prev;
        });
    }

    // TODO ha az egyik sizerequest constraintsje ugyanaz mint BoxConstraints.tight(surface.size()),
    //      akkor nem kéne feleslegesen kétszer kiszámolni

    public interface LayoutHelperDelegate {

        @NonNull Widget computePreferredSize(BoxLayoutResult.SizeRequest sizeRequest);

        @NonNull Widget computeChildArrangement(Size containerSize);
    }
}
