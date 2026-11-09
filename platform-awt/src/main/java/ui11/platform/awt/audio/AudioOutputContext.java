package ui11.platform.awt.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.Widget;
import ui11.observable.Scope;
import ui11.provide.Provide;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AudioOutputContext extends Widget {

    private final Widget content;

    @Remember private Mixer mixer;

    public AudioOutputContext(Widget content) {
        this.content = content;
    }

    @Override
    protected void initState() {
        mixer = new Mixer();
    }

    @Override
    protected void onResume() {
        mixer.start();
        // TODO stop?
    }

    @Provide
    private Mixer mixer() {
        return mixer;
    }

    @Override
    protected Widget build() {
        return content;
    }

    public static class Mixer {

        private static final Logger logger = LoggerFactory.getLogger(Mixer.class);

        private final Object buffersLock = new Object();
        /* @GuardedBy("buffersLock") */ private final Set<AudioBuffer> buffers = new HashSet<>();

        // TODO amíg egy widget tree refresh fut, addig az add/remove-okat queueolni kéne,
        //      mert anélkül félkész hangok képződhetnek

        public void add(AudioBuffer buffer, Scope scope) {
            synchronized (buffersLock) {
                if (!buffers.add(buffer))
                    throw new RuntimeException("buffer already added");
                buffersLock.notifyAll();
            }
            scope.onClose(() -> {
                synchronized (buffersLock) {
                    if (!buffers.remove(buffer))
                        throw new RuntimeException("buffer already removed");
                }
            });
        }

        void start() {
            Thread.ofPlatform().name("JavaSound Audio Mixer").daemon().start(this::run);
        }

        private void run() {
            try {
                // TODO ez lehet hogy menet közben megváltozik?
                ByteOrder byteOrder = ByteOrder.nativeOrder();
                SourceDataLine l = AudioSystem.getSourceDataLine(new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 2, 8000,
                        byteOrder == ByteOrder.BIG_ENDIAN));
                l.open(l.getFormat());
                l.start();
                int maxSamples = 1000;
                ByteBuffer mixResults = ByteBuffer.allocate(l.getFormat().getChannels() * maxSamples * Float.BYTES).
                        order(byteOrder);
                while (true) {
                    Collection<AudioBuffer> buffersSnapshot;
                    synchronized (buffersLock) {
                        while (true) {
                            buffersSnapshot = new ArrayList<>(buffers);
                            if (buffersSnapshot.isEmpty()) {
                                System.out.println("no audio buffers");
                                buffersLock.wait();
                            } else
                                break;
                        }
                    }
                    // TODO lehet hogy egy input buffert azóta már elvettek tőlünk át más mixer olvassa
                    int samples = maxSamples;
                    for (AudioBuffer b : buffersSnapshot)
                        samples = Math.min(samples, b.waitUntilReadPermitted());
                    assert samples > 0;
                    mixResults.clear();
                    for (int i = 0; i < samples; i++) {
                        float val = 0;
                        for (AudioBuffer b : buffersSnapshot)
                            val += b.read();
                        mixResults.putShort((short) (val * Short.MAX_VALUE)); // TODO mi van Short.MIN_VALUE-val?
                    }
                    int write = l.write(mixResults.array(), 0, mixResults.position());
                    if (write != mixResults.position())
                        throw new RuntimeException("not all data was written to SourceDataLine: " + mixResults + ", " + write);
                }
            } catch (Throwable e) {
                logger.error("Failure in audio mixer thread", e);
            }
        }
    }
}
