package ui11.animation;

import ui11.observable.Scope;
import ui11.observable.SimpleScope;
import ui11.animation.PlaybackController.AnimationPlaybackPeer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TimelineBuilder {

    private Duration currentTime = Duration.ZERO;
    private final List<Action> actions = new ArrayList<>();

    private boolean firstStart;

    public TimelineBuilder(PlaybackController playbackController, Scheduler scheduler, Scope scope) {
        AnimationPlaybackPeer p = new AnimationPlaybackPeer() {

            private SimpleScope playbackScope;

            @Override
            public void playFrom(Duration duration, boolean loop) {
                if (playbackScope != null)
                    throw new RuntimeException("already started");

                firstStart = true;
                begin(duration, loop);
            }

            private void begin(Duration duration, boolean loop) {
                playbackScope = new SimpleScope(scope);
                for (Action action : actions) {
                    schedule(duration, action.pos, action.task);
                }
                if (loop) {
                    Duration length = currentTime;
                    schedule(duration, length, () -> begin(Duration.ZERO, true));
                }
            }

            private void schedule(Duration offset, Duration pos, Runnable task) {
                Duration delay = pos.minus(offset);
                if (delay.isNegative())
                    delay = Duration.ZERO;
                scheduler.scheduleOneTime(delay, task, playbackScope);
            }

            @Override
            public void stop() {
                if (playbackScope == null)
                    throw new RuntimeException("not started");
                playbackScope.close();
                playbackScope = null;
            }
        };
        playbackController.addPeer(p);
        scope.onClose(() -> playbackController.removePeer(p));
    }

    public TimelineBuilder after(Duration duration) {
        if (duration.isNegative() || duration.isZero())
            throw new IllegalArgumentException("duration must be positive");
        if (firstStart)
            throw new IllegalStateException("can't modify TimelineBuilder after playback started");
        currentTime = currentTime.plus(duration);
        return this;
    }

    public TimelineBuilder action(Runnable action) {
        if (firstStart)
            throw new IllegalStateException("can't modify TimelineBuilder after playback started");
        actions.add(new Action(action, currentTime));
        return this;
    }

    private record Action(Runnable task, Duration pos) {
    }
}
