package ui11.animation.lottie.web;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.media.LottieView;
import ui11.platform.dom.DOMElementWidget;

public class LottieWebPlayerProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.add(ResolverRegistry.Priority.EMULATED_BY_NATIVE, LottieView.class, LottieWebAnimationPeer::new).
                offers(DOMElementWidget.class);
    }
}
