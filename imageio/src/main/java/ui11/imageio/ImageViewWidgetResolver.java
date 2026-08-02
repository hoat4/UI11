package ui11.imageio;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequestor;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.media.JPEGImageView;
import ui11.WidgetResolver;

import org.jspecify.annotations.Nullable;

public class ImageViewWidgetResolver extends WidgetResolver {

    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        return switch (widget) {
            case JPEGImageView jpg -> new ImageViewImpl(jpg.source());
            default -> null;
        };
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget, PeerRequestor.@NonNull Request<?> request) {
        return null;
    }
}
