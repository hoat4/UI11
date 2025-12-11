package ui11.audio.io;

import ui11.audio.spi.AudioProvider;
import ui11.audio.buffer.ReadableAudioBuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.ServiceLoader;

public class AudioIO {

    private AudioIO() {
        throw new Error();
    }

    /**
     * Nem zárja be a megadott InputStreamet
     */
    // TODO exceptionök?
    public static ReadableAudioBuffer decode(InputStream in) throws IOException {
        return ServiceLoader.load(AudioProvider.class).iterator().next().decodeFully(in);
    }

    public static ReadableAudioBuffer decode(byte[] bytes) {
        try {
            return decode(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            // TODO ByteArrayInputStream can't throw IOException
            throw new RuntimeException(e);
        }
    }

    // TODO ByteBuffer
}
