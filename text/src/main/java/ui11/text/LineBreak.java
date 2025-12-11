package ui11.text;

import ui11.SubstitutedWidget;
import ui11.Widget;

import javax.annotation.Nonnull;

public final class LineBreak extends SubstitutedWidget {

    public LineBreak() {
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        return new Text("\n");
    }
}
