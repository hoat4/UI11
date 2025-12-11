package ui11.audio.spi;

import ui11.audio.AudioDestination;
import ui11.audio.AudioSourceNode;
import ui11.audio.buffer.ReadableAudioBuffer;

import java.io.IOException;
import java.io.InputStream;

public interface AudioProvider {

    /**
     * Nem zárja be a megadott InputStreamet
     */
    ReadableAudioBuffer decodeFully(InputStream in) throws IOException;

    AudioSourceNode createSourceNode(ReadableAudioBuffer buffer);

    // TODO ez eltérő vagy azonos objektumot ad vissza?
    AudioDestination defaultOutputDevice();
}
