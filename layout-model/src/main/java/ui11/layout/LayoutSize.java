package ui11.layout;

import ui11.geom.Axis;
import ui11.geom.Size;
import ui11.geom.Length;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

// ehelyett inkább kéne egy BoxConstraints-szerűség, double helyett Length-tel, de végtelent is lehetővé téve
public record LayoutSize(@Nullable Length width, @Nullable Length height) {

    public Length length(Axis axis) {
        return switch(axis) {
            case HORIZONTAL -> width;
            case VERTICAL -> height;
        };
    }

    public static LayoutSize px(Size size) {
        return new LayoutSize(Length.px(size.width()), Length.px(size.height()));
    }
}
