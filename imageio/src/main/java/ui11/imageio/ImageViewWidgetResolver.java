package ui11.imageio;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.media.JPEGImageView;
import ui11.WidgetResolver;

import org.jspecify.annotations.Nullable;

public class ImageViewWidgetResolver extends WidgetResolver {

    @Override
    protected Class<? extends SubstitutedWidget> supportedTargetType() {
        return SubstitutedWidget.class;
    }

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget) {
        return switch (widget) {
            case JPEGImageView jpg -> new ImageViewImpl(jpg.source());
            default -> null;
        };
    }
}
