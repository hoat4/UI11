package ui11.decoration;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.color.Color;
import ui11.layout.singlechild.Padding;

import org.jspecify.annotations.NonNull;

import static ui11.graphics.Empty.empty;
import static ui11.decoration.Background.withBackground;
import static ui11.geom.Length.px;

public final class Separator extends SubstitutedWidget {

    @Override
    protected @NonNull Widget fallbackContent() {
        return withBackground(Color.GRAY, Padding.atTop(px(1), empty()));
    }
}
