package ui11.animation.lottie;

import ui11.SubstitutedWidget;
import ui11.animation.PlaybackController;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class LottieAnimation extends SubstitutedWidget {

    @Nonnull private final LottieAnimationData animationData;
    @Nonnull private final PlaybackController playbackController;

    public LottieAnimation(LottieAnimationData animationData,
                           PlaybackController playbackController) {
        this.animationData = Objects.requireNonNull(animationData);
        this.playbackController = Objects.requireNonNull(playbackController);
    }

    public LottieAnimation(LottieAnimationData animationData) {
        this(animationData, new PlaybackController());
        playbackController.playAndLoop();
    }

    @Nonnull
    public LottieAnimationData animationData() {
        return animationData;
    }

    @Nonnull
    public PlaybackController playbackController() {
        return playbackController;
    }
}
