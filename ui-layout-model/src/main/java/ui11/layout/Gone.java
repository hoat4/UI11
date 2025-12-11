package ui11.layout;

import ui11.Widget;
import ui11.provide.UpValue;
import ui11.provide.UpValueWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static ui11.graphics.Empty.empty;

public class Gone extends Widget implements UpValue {

    private Gone() {
    }

    public static Gone gone() {
        return new Gone();
    }

    public static @Nonnull Widget goneIfNull(@Nullable Widget w) {
        return w == null ? gone() : w;
    }

    @Override
    protected Widget build() {
        return new UpValueWrapper(this, empty());
    }

    @Override
    public String toString() {
        return "Gone";
    }
}
