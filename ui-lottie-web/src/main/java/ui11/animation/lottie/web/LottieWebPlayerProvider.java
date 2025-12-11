package ui11.animation.lottie.web;

import ui11.Widget;
import ui11.animation.lottie.LottieAnimation;
import ui11.platform.dom.DOMEnvironment;
import ui11.resolution.WidgetResolver;

import javax.annotation.Nullable;

public class LottieWebPlayerProvider implements WidgetResolver {
    @Nullable
    @Override
    public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
        if (widget instanceof LottieAnimation animation)
            return new LottieWebAnimationPeer(
                    resolutionContext.inherited(DOMEnvironment.class),
                    animation);
        else
            return null;
    }
}
