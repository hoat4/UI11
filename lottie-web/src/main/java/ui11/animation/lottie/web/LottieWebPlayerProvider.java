package ui11.animation.lottie.web;

import org.jspecify.annotations.NonNull;
import ui11.PeerRequestor;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.media.LottieView;
import ui11.WidgetResolver;

import org.jspecify.annotations.Nullable;
import ui11.platform.dom.DOMPeerBase;

public class LottieWebPlayerProvider extends WidgetResolver {
    @Override
    protected @Nullable Widget tryResolveGeneric(@NonNull SubstitutedWidget widget) {
        if (widget instanceof LottieView animation)
            return new LottieWebAnimationPeer(animation);
        else
            return null;
    }

    @Override
    protected @Nullable Widget tryResolveRequestSpecific(@NonNull SubstitutedWidget widget,
                                                         PeerRequestor.@NonNull Request<?> request) {
        return null;
    }
}
