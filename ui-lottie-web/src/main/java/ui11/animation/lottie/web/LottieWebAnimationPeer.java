package ui11.animation.lottie.web;

import org.teavm.jso.dom.events.Registration;
import org.teavm.jso.dom.html.HTMLElement;
import ui11.Widget;
import ui11.animation.PlaybackController;
import ui11.animation.PlaybackController.AnimationPlaybackPeer;
import ui11.animation.lottie.LottieAnimation;
import ui11.platform.dom.DOMElementWidget;
import ui11.platform.dom.DOMEnvironment;

import java.time.Duration;

public class LottieWebAnimationPeer extends Widget implements AnimationPlaybackPeer {

    private final DOMEnvironment domEnv;
    private final LottieAnimation widget;

    @State private PlaybackController lastContext;
    @State private LottieWebPlayerAPI.LottieAnimation nativeAnimation;
    @State private Registration completionListener;

    public LottieWebAnimationPeer(DOMEnvironment domEnv, LottieAnimation widget) {
        this.domEnv = domEnv;
        this.widget = widget;
    }

    @Override
    protected void initState() {
    }

    @Override
    protected void onResume() {
        untilPause().onClose(() -> {
            if (lastContext != null) {
                lastContext.removePeer(this);
                lastContext = null;
            }
            if (nativeAnimation != null) {
                nativeAnimation.destroy();
                nativeAnimation = null;
            }
        });
    }

    @Override
    protected Widget build() {
        LottieWebAnimationData animationData = (LottieWebAnimationData) widget.animationData();

        HTMLElement elem = domEnv.document.createElement("div");
        if (nativeAnimation != null)
            nativeAnimation.destroy();
        nativeAnimation = LottieWebPlayerAPI.loadAnimation(elem, LottieWebPlayerAPI.RENDERER_SVG,
                false, false, animationData.obj);

        if (widget.playbackController() != lastContext) {
            if (lastContext != null)
                lastContext.removePeer(this);
            (lastContext = widget.playbackController()).addPeer(this);
        }

        return new DOMElementWidget(elem);
    }

    @Override
    public void playFrom(Duration duration, boolean loop) {
        if (completionListener != null) {
            completionListener.dispose();
            completionListener = null;
        }
        nativeAnimation.goToAndPlay(duration.toMillis(), false);
        // TODO mi van ha 0 hosszúságú az animáció? lehetséges olyan?
        if (loop) {
            completionListener = nativeAnimation.onEvent(LottieWebPlayerAPI.LottieAnimation.EVENT_COMPLETE, evt -> {
                playFrom(Duration.ZERO, true);
            });
        }
    }

    @Override
    public void stop() {
        if (completionListener != null) {
            completionListener.dispose();
            completionListener = null;
        }
        nativeAnimation.stop();
    }

    static {
        LottieWebPlayerAPI.ensureLoaded();
    }
}
