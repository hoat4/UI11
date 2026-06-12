package ui11.layout;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class Gone extends SubstitutedWidget {

    private static final Gone INSTANCE = new Gone();

    private Gone() {
        if (INSTANCE != null)
            throw new Error();
    }

    public static Gone gone() {
        return INSTANCE;
    }

    public static @NonNull Widget goneIfNull(@Nullable Widget w) {
        return w == null ? gone() : w;
    }

    @Override
    public String toString() {
        return "Gone";
    }
}
