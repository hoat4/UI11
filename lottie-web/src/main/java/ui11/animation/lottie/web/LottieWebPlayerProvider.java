package ui11.animation.lottie.web;

import org.jspecify.annotations.NonNull;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.media.LottieView;
import ui11.WidgetResolver;

import org.jspecify.annotations.Nullable;
import ui11.platform.dom.DOMElementHolder;

public class LottieWebPlayerProvider extends WidgetResolver {

    @Override
    protected Class<? extends SubstitutedWidget> supportedTargetType() {
        return DOMElementHolder.class;
    }

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget) {
        if (widget instanceof LottieView animation)
            return new LottieWebAnimationPeer(animation);
        else
            return null;
    }
}
