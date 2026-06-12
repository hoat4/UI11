package ui11.animation.lottie.web;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.media.LottieView;
import ui11.platform.dom.DOMEnvironment;
import ui11.resolution.PeerCreationRequest;
import ui11.resolution.WidgetResolver;

import org.jspecify.annotations.Nullable;

public class LottieWebPlayerProvider implements WidgetResolver {
    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
        if (widget instanceof LottieView animation)
            return new LottieWebAnimationPeer(animation);
        else
            return null;
    }
}
