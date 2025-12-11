package ui11.platform.dom;

import ui11.audio.AudioDestination;
import ui11.audio.AudioNode;
import ui11.audio.spi.AudioProvider;
import ui11.audio.AudioSourceNode;
import ui11.audio.buffer.ReadableAudioBuffer;
import org.teavm.jso.typedarrays.ArrayBuffer;
import org.teavm.jso.typedarrays.Uint8Array;
import org.teavm.jso.webaudio.AudioBuffer;
import org.teavm.jso.webaudio.AudioBufferSourceNode;
import org.teavm.jso.webaudio.AudioContext;

import java.io.IOException;
import java.io.InputStream;

public class WebAudioAudioProvider implements AudioProvider {

    private final AudioContext decodingAudioContext = new AudioContext();

    @Override
    public ReadableAudioBuffer decodeFully(InputStream in) throws IOException {
        byte[] bytes = in.readAllBytes();
        ArrayBuffer arrayBuffer = new ArrayBuffer(bytes.length);
        Uint8Array a = new Uint8Array(arrayBuffer);
        a.set(bytes);
        // TODO az 1 paraméteres MDN szerint Promise-t ad vissza, TeaVM-ben viszont AudioBuffer van
        ReadableAudioBufferImpl readableAudioBuffer = new ReadableAudioBufferImpl();
        decodingAudioContext.decodeAudioData(arrayBuffer, decodedData -> {
            readableAudioBuffer.jsAudioBuffer = decodedData;
        });
        return readableAudioBuffer;
    }

    @Override
    public AudioSourceNode createSourceNode(ReadableAudioBuffer buffer) {
        // TODO mi legyen ha nem sikerült a cast? meg a többi függvénynél is
        AudioBuffer decodePromise = ((ReadableAudioBufferImpl) buffer).jsAudioBuffer;
        return new SourceNodeImpl(decodePromise);
    }

    @Override
    public AudioDestination defaultOutputDevice() {
        return new DestinationImpl();
    }

    private static class ReadableAudioBufferImpl implements ReadableAudioBuffer {

        AudioBuffer jsAudioBuffer; // null, ha még nem töltött be
    }

    private static class SourceNodeImpl implements AudioSourceNode {

        private final AudioBuffer audioBuffer;

        public SourceNodeImpl(AudioBuffer audioBuffer) {
            this.audioBuffer = audioBuffer;
        }
    }

    private static class DestinationImpl implements AudioDestination {

        @Override
        public void play(AudioNode node) {
            AudioContext ctx = new AudioContext();
            AudioBufferSourceNode audioBufferSourceNode = ctx.createBufferSource();
            audioBufferSourceNode.setBuffer(((SourceNodeImpl) node).audioBuffer);
            audioBufferSourceNode.connect(ctx.getDestination());
            audioBufferSourceNode.start();
        }
    }
}