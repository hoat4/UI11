package ui11.decoration;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.decoration.Box.BorderSpec;
import ui11.graphics.fill.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Insets;
import ui11.geom.Length;

import javax.annotation.Nonnull;
import java.util.Objects;

import static ui11.geom.Length.px;

// CSS-ben rendering sorrend: background, majd border, majd content
// tehát ha kilóg a content, akkor a bordert el fogja fedni

public final class Border extends SubstitutedWidget {

    @Nonnull private final Insets thicknesses;
    @Nonnull private final Widget stroke;
    @Nonnull private final Widget content;

    public Border(@Nonnull Insets thicknesses, @Nonnull Widget stroke, @Nonnull Widget content) {
        this.thicknesses = Objects.requireNonNull(thicknesses);
        this.stroke = Objects.requireNonNull(stroke);
        this.content = Objects.requireNonNull(content);
    }

    public Border(Length thickness, Color stroke, Widget content) {
        this(thickness, new ColorFill(stroke), content);
    }

    public Border(Insets thicknesses, Color stroke, Widget content) {
        this(thicknesses, new ColorFill(stroke), content);
    }

    public Border(Length thickness, Widget stroke, Widget content) {
        this(Insets.all(thickness), stroke, content);
    }

    public Insets thicknesses() {
        return thicknesses;
    }

    public Widget stroke() {
        return stroke;
    }

    public Widget content() {
        return content;
    }

    @Nonnull
    @Override
    protected Widget fallbackContent() {
        // TODO ezeket a widget instanceof ...-okat meg kéne szüntetni,
        //      mert ha egy lényegtelen Tag az értékük, akkor azokat nem kell nézni

        // cornerRadius == 0-t azért kell nézni, mert ha RoundedCorners-en kívülre
        // rakunk egy Bordert, akkor a bordernek nem lekerítettnek kell lennie
        return content instanceof Box b && b.border() == null && b.cornerRadius().isZero() ?
                b.withBorder(new BorderSpec(thicknesses, stroke)) :
                new Box(content).withBorder(new BorderSpec(thicknesses, stroke));
    }

    public static Widget atTop(Color stroke, Widget content) {
        return new Border(Insets.atTop(px(1)), new ColorFill(stroke), content);
    }

    public static Widget atRight(Color stroke, Widget content) {
        return new Border(Insets.atRight(px(1)), new ColorFill(stroke), content);
    }

    public static Widget atBottom(Color stroke, Widget content) {
        return new Border(Insets.atBottom(px(1)), new ColorFill(stroke), content);
    }

    public static Widget atLeft(Color stroke, Widget content) {
        return new Border(Insets.atLeft(px(1)), new ColorFill(stroke), content);
    }
}
