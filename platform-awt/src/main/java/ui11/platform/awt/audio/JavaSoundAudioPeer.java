package ui11.platform.awt.audio;

import ui11.Widget;
import ui11.media.Audio;
import ui11.media.MediaSource;

import java.util.concurrent.atomic.AtomicInteger;

import static ui11.graphics.Empty.empty;

public class JavaSoundAudioPeer extends Widget {

    private static final AtomicInteger THREAD_NAME_GEN = new AtomicInteger();

    private final Audio audio;

    @Inject private AudioOutputContext.Mixer mixer;

    @Remember private MediaSource source;
    @Remember private AudioBuffer buffer;
    @Remember private Thread decodeThread;

    public JavaSoundAudioPeer(Audio audio) {
        this.audio = audio;
    }

    @Override
    protected Widget build() {
        if (!audio.source().equals(source)) {
            buffer = new AudioBuffer(1, 1000 /* TODO */);
            source = audio.source();
            // TODO előző decode threadet meg kéne állítani source változáskor és pausekor is
            decodeThread = Thread.ofPlatform().
                    name("Audio decode thread #"+THREAD_NAME_GEN.incrementAndGet()).
                    start(new AudioDecodeThread(source, buffer));
        }

        mixer.add(buffer, untilNextRebuild());

        return empty();
    }

}
