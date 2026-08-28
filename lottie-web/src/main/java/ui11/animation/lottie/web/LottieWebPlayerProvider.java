package ui11.animation.lottie.web;

import ui11.ResolverProvider;
import ui11.ResolverRegistry;
import ui11.media.LottieView;
import ui11.platform.dom.DOMPeerBase;

public class LottieWebPlayerProvider implements ResolverProvider {

    @Override
    public void configure(ResolverRegistry r) {
        r.addPeerIndependentWithFilter(DOMPeerBase.DOMPeerCreationRequest.class,
                LottieView.class, LottieWebAnimationPeer::new);
    }
}
