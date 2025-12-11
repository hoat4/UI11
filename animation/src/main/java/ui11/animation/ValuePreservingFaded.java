package ui11.animation;

import ui11.*;

import java.util.Objects;
import java.util.function.Function;

import static ui11.graphics.Empty.empty;

// TODO név
public class ValuePreservingFaded<T> extends Widget {

    private final boolean visible;
    private final T value;
    private final Function<T, Widget> contentFactory;

    @State private T lastValueWhenWasVisible;
    @State private boolean neverWasVisible;

    public ValuePreservingFaded(boolean visible, T value, Function<T, Widget> contentFactory) {
        this.visible = visible;
        this.value = value;
        this.contentFactory = contentFactory;
        Objects.requireNonNull(contentFactory);
    }

    @Override
    protected void initState() {
        neverWasVisible = true;
    }

    @Override
    protected Widget build() {
        if (visible) {
            lastValueWhenWasVisible = value;
            neverWasVisible = false;
        }
        return new Faded(visible,
                neverWasVisible ? empty() :
                        contentFactory.apply(lastValueWhenWasVisible));
    }
}
