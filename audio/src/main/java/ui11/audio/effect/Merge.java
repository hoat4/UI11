package ui11.audio.effect;

import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.*;

public final class Merge extends SubstitutedWidget {

    // TODO this should be freezed somehow
    private final List<Widget> inputs = new ArrayList<>();

    public Merge(Widget... inputs) {
        for (Widget input : inputs)
            Objects.requireNonNull(input);
        this.inputs.addAll(Arrays.asList(inputs));
    }

    public List<Widget> inputs() {
        return inputs;
    }
}
