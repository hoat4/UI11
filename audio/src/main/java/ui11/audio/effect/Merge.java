package ui11.audio.effect;

import ui11.audio.AudioNode;

import java.util.*;

public final class Merge implements AudioNode {

    private final List<AudioNode> inputs = new ArrayList<>();

    public Merge(AudioNode... inputs) {
        for (AudioNode input : inputs)
            Objects.requireNonNull(input);
        this.inputs.addAll(Arrays.asList(inputs));
    }

    public List<AudioNode> inputs() {
        return inputs;
    }
}
