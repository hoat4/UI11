package ui11.text;

import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

public final class LineBreak extends SubstitutedWidget {

    private static final LineBreak INSTANCE = new LineBreak();
    private static final Text FALLBACK = new Text("\n");

    private LineBreak() {
        if (INSTANCE != null)
            throw new Error();
    }

    public static LineBreak lineBreak() {
        return INSTANCE;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return FALLBACK;
    }
}
