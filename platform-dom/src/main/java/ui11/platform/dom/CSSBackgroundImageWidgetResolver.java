package ui11.platform.dom;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.WidgetResolver;
import ui11.media.JPEGImageView;
import ui11.media.SVGImageView;
import ui11.platform.dom.peers.DOMCoverPeer;

class CSSBackgroundImageWidgetResolver extends WidgetResolver {

    @Override
    protected Class<? extends SubstitutedWidget> supportedTargetType() {
        return DOMCoverPeer.CSSBackgroundImage.class;
    }

    @Override
    protected @Nullable Widget resolveOrNull(@NonNull Widget widget) {
        return switch (widget) {
            case SVGImageView svg -> {
                if (svg.isInteractive())
                    throw new RuntimeException("interactive SVGImageView inside CSSBackgroundImageContext");
                if (!svg.embeddedWidgets().isEmpty())
                    throw new RuntimeException("embedded widgets in SVG inside CSSBackgroundImageContext");
                yield new DOMCoverPeer.CSSBackgroundImage(svg.source().toURI());
            }
            case JPEGImageView jpg -> new DOMCoverPeer.CSSBackgroundImage(jpg.source().toURI());
            default -> null;
        };
    }
}
