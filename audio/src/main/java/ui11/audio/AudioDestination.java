package ui11.audio;

import ui11.audio.spi.AudioProvider;

import java.util.ServiceLoader;

public interface AudioDestination extends AudioNode {

    static AudioDestination getDefaultSpeaker() {
        // TODO 0 vagy 1-nél több provider esetén mi legyen?
        return ServiceLoader.load(AudioProvider.class).iterator().next().defaultOutputDevice();
    }

    void play(AudioNode node);

    // TODO play egyszerre több destinationön?
}
