package ui11.animation.lottie.web;

import org.jspecify.annotations.NonNull;
import ui11.*;
import ui11.media.LottieView;

import org.jspecify.annotations.Nullable;

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
                                                         @NonNull PeerRequest<?> request) {
        return null;
    }
}
