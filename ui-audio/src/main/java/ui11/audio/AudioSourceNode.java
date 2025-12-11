package ui11.audio;

import ui11.audio.buffer.ReadableAudioBuffer;
import ui11.audio.spi.AudioProvider;

import java.util.ServiceLoader;

public interface AudioSourceNode extends AudioNode {

    static AudioSourceNode forBuffer(ReadableAudioBuffer buffer) {
        // TODO 0 vagy 1-nél több provider?
        return ServiceLoader.load(AudioProvider.class).iterator().next().createSourceNode(buffer);
    }
}
