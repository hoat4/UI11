package ui11.renderer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.Size;
import ui11.text.TextStyle;

public interface TextRenderer {

    @NonNull TextLayoutCalculator createTextLayoutCalculator(@Nullable TextLayoutCalculator textLayoutCalculator);

    /**
     * If only a single (or few) property will change (either text or a property in text style),
     * TextLayoutCalculator should be reused and not shared with others.
     * <p>
     * Not thread safe.
     */
    interface TextLayoutCalculator {

        @NonNull TextLayout computeTextLayout(@NonNull String text, @NonNull TextStyle textStyle);
    }

    interface TextLayout {

        Size size();
    }
}
