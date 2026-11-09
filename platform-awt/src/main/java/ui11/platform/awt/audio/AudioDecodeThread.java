package ui11.platform.awt.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ui11.media.MediaSource;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class AudioDecodeThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AudioDecodeThread.class);

    private final MediaSource source;
    private final AudioBuffer buffer;

    public AudioDecodeThread(MediaSource source, AudioBuffer buffer) {
        this.source = source;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            AudioInputStream ain;
            if (source instanceof MediaSource.InlineBinarySource bytes) {
                ain = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes.bytes()));
            } else {
                ain = AudioSystem.getAudioInputStream(source.toURI().toURL());
            }
            ain = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_FLOAT, ain);
            ByteBuffer b = ByteBuffer.allocate(ain.getFormat().getFrameSize() * buffer.sampleCount());
            b.order(ain.getFormat().isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            if (ain.getFormat().getSampleSizeInBits() != 32)
                throw new RuntimeException("Sample bit size not 32 for PCM_FLOAT: " + ain.getFormat().getSampleSizeInBits());
            for (int readBytes; (readBytes = ain.read(b.array(), 0, buffer.waitUntilWritePermitted())) > 0; )
                buffer.write(b.limit(readBytes).asFloatBuffer());
            ain.close();
        } catch (Throwable e) {
            logger.error("Can't decode audio", e);
        }
    }
}
