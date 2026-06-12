package ui11.imageio;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.media.JPEGImageView;
import ui11.resolution.PeerCreationRequest;
import ui11.resolution.WidgetResolver;

import org.jspecify.annotations.Nullable;

public class ImageViewWidgetResolver implements WidgetResolver {

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
        return switch (widget) {
            case JPEGImageView jpg -> new ImageViewImpl(jpg.source());
            default -> null;
        };
    }
}
