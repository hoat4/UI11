package ui11.animation.lottie.web;

import org.teavm.jso.JSObject;
import org.teavm.jso.dom.events.Registration;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.json.JSON;
import ui11.Widget;
import ui11.animation.PlaybackController;
import ui11.animation.PlaybackController.AnimationPlaybackPeer;
import ui11.media.ImageSource.InlineStringSource;
import ui11.media.LottieView;
import ui11.observable.Observable;
import ui11.platform.dom.DOMElementWidget;
import ui11.platform.dom.DOMEnvironment;

import java.time.Duration;

public class LottieWebAnimationPeer extends Widget implements AnimationPlaybackPeer {

    private final LottieView widget;

    @Inject private DOMEnvironment domEnv;

    @Remember private PlaybackController lastContext;
    @Remember private LottieWebPlayerAPI.LottieAnimation nativeAnimation;
    @Remember private Registration completionListener;
    @Remember private String lastJsonStr;
    @Remember private JSObject lastJsonParsed;

    public LottieWebAnimationPeer(LottieView widget) {
        this.widget = widget;
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
        if (!(widget.source() instanceof InlineStringSource inlineStringSource))
            throw new RuntimeException("TODO "+widget);

        if (!inlineStringSource.content().equals(lastJsonStr)) {
            lastJsonParsed = JSON.parse(inlineStringSource.content());
            lastJsonStr = inlineStringSource.content();
        }

        HTMLElement elem = domEnv.document.createElement("div");
        if (nativeAnimation != null)
            nativeAnimation.destroy();
        nativeAnimation = LottieWebPlayerAPI.loadAnimation(elem, LottieWebPlayerAPI.RENDERER_SVG,
                widget.playbackController() == null,
                widget.playbackController() == null,
                lastJsonParsed);

        if (widget.playbackController() != lastContext) {
            if (lastContext != null)
                lastContext.removePeer(this);
            lastContext = widget.playbackController();
            if (lastContext != null)
                lastContext.addPeer(this);
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
