package ui11.audio.spi;

import ui11.audio.AudioDestination;
import ui11.audio.AudioNode;

import java.io.IOException;
import java.io.InputStream;

public interface AudioProvider {

    /**
     * Nem zárja be a megadott InputStreamet
     */
    AudioNode decodeFully(InputStream in) throws IOException;

    // TODO ez eltérő vagy azonos objektumot ad vissza?
    AudioDestination defaultOutputDevice();
}
