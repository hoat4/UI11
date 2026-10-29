package ui11.audio.io;

import ui11.audio.AudioNode;
import ui11.audio.spi.AudioProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

public class AudioIO {

    private AudioIO() {
        throw new Error();
    }

    /**
     * Nem zárja be a megadott InputStreamet
     */
    // TODO exceptionök?
    public static AudioNode decode(InputStream in) throws IOException {
        return ServiceLoader.load(AudioProvider.class).iterator().next().decodeFully(in);
    }

    public static AudioNode decode(byte[] bytes) {
        try {
            return decode(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            // TODO ByteArrayInputStream can't throw IOException
            throw new RuntimeException(e);
        }
    }

    // TODO ByteBuffer
}
