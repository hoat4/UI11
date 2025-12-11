package ui11.animation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PlaybackController {

    private final List<AnimationPlaybackPeer> peers = new ArrayList<>();
    private Instant startedAt;
    private boolean loop;

    public void play() {
        if (startedAt == null)
            playFromStart();
    }

    public void playAndLoop() {
        if (startedAt == null)
            playFromStartAndLoop();
    }

    public void playFromStart() {
        loop = false;
        playImpl();
    }

    public void playFromStartAndLoop() {
        loop = true;
        playImpl();
    }

    private void playImpl() {
        startedAt = Instant.now();
        peers.forEach(p -> p.playFrom(Duration.ZERO, loop));
    }

    public void stop() {
        if (startedAt != null)
            peers.forEach(AnimationPlaybackPeer::stop);
    }

    // TODO add/remove helyett scopeok
    public void addPeer(AnimationPlaybackPeer p) {
        peers.add(p);
        if (startedAt != null)
            p.playFrom(Duration.between(startedAt, Instant.now()), loop);
    }

    public void removePeer(AnimationPlaybackPeer p) {
        peers.remove(p);
    }

    public interface AnimationPlaybackPeer {

        /**
         * Ha már megy, akkor is ugorjon a megadott pontra.
         */
        // TODO mi történjen ha duration kívül van az animáció hosszán?
        void playFrom(Duration duration, boolean loop);

        void stop();
    }
}
