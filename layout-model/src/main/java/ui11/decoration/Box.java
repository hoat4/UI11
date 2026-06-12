package ui11.decoration;

import ui11.resolution.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;
import ui11.color.Color;
import ui11.graphics.fill.ColorFill;
import ui11.layout.Insets;
import ui11.layout.LayoutSize;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

import static ui11.geom.Length.px;

// TODO cornerRadiusból 4 kéne
// mi van akkor ha van minSize és fixedSize is?
@Deprecated
public final class Box extends SubstitutedWidget {

    private final Widget content;
    @Nullable private final Widget background;
    @Nullable private final BorderSpec border;
    @Nullable private final BoxShadow boxShadow;
    @Nullable private final LayoutSize minSize;
    @Nullable private final LayoutSize fixedSize;
    private final Length cornerRadius;

    /**
     * @param content
     * @param background   ennek a preferred size-ja ignorálva lesz
     * @param border
     * @param boxShadow
     * @param minSize      ez magában foglalja a bordert is
     * @param fixedSize    ez magában foglalja a bordert is
     * @param cornerRadius
     */
    public Box(Widget content, @Nullable Widget background,
               @Nullable BorderSpec border, @Nullable BoxShadow boxShadow,
               @Nullable LayoutSize minSize, @Nullable LayoutSize fixedSize,
               Length cornerRadius) {
        this.content = content;
        this.background = background;
        this.border = border;
        this.boxShadow = boxShadow;
        this.minSize = minSize;
        this.fixedSize = fixedSize;
        this.cornerRadius = cornerRadius;
    }

    public Box(Widget content) {
        this(content, null, null, null, null, null, Length.zero());
    }

    public Widget content() {
        return content;
    }

    @Nullable
    public Widget background() {
        return background;
    }

    @Nullable
    public BorderSpec border() {
        return border;
    }

    @Nullable
    public BoxShadow boxShadow() {
        return boxShadow;
    }

    @Nullable
    public LayoutSize minSize() {
        return minSize;
    }

    @Nullable
    public LayoutSize fixedSize() {
        return fixedSize;
    }

    public Length cornerRadius() {
        return cornerRadius;
    }

    /**
     * @param background nem feltétlen fog pointereventeket kapni (legalábbis DOMBoxPeer esetén most (2025-01) így van)
     */
    public Box withBackground(Widget background) {
        return new Box(content, background, border, boxShadow, minSize, fixedSize, cornerRadius);
    }

    public Box withBackground(Color background) {
        return withBackground(new ColorFill(background));
    }

    public Box withBoxShadow(BoxShadow boxShadow) {
        return new Box(content, background, border, boxShadow, minSize, fixedSize, cornerRadius);
    }

    public Box withBorder(BorderSpec border) {
        return new Box(content, background, border, boxShadow, minSize, fixedSize, cornerRadius);
    }

    public Box withBorder(Widget fill) {
        return withBorder(new BorderSpec(Insets.all(px(1)), fill));
    }

    public Box withBorder(Length thickness, Color color) {
        return withBorder(thickness, new ColorFill(color));
    }

    public Box withBorder(Length thickness, Widget fill) {
        return withBorder(new BorderSpec(Insets.all(thickness), fill));
    }

    public Box withMinSize(Length width, Length height) {
        return withMinSize(new LayoutSize(width, height));
    }

    public Box withMinSize(LayoutSize minSize) {
        return new Box(content, background, border, boxShadow, minSize, fixedSize, cornerRadius);
    }

    public Box withFixedSize(Length width, Length height) {
        return withFixedSize(new LayoutSize(width, height));
    }

    public Box withFixedSize(LayoutSize fixedSize) {
        return new Box(content, background, border, boxShadow, minSize, fixedSize, cornerRadius);
    }

    public Box withCornerRadius(Length cornerRadius) {
        return new Box(content, background, border, boxShadow, minSize, fixedSize, cornerRadius);
    }

    public static Widget withRoundedBorder(Color color, Length cornerRadius, Widget widget) {
        return new Box(widget).withBorder(px(1), new ColorFill(color)).withCornerRadius(cornerRadius);
    }

    public static Widget withMinWidth(Length minWidth, Widget widget) {
        return new Box(widget).withMinSize(minWidth, null);
    }

    public static Widget withMinHeight(Length minHeight, Widget widget) {
        return new Box(widget).withMinSize(null, minHeight);
    }

    public static Widget withMinSize(Length minWidth, Length minHeight, Widget widget) {
        return new Box(widget).withMinSize(minWidth, minHeight);
    }

    public static Widget withSizeAndRoundedCorners(LayoutSize size, Length cornerRadius, Widget widget) {
        Objects.requireNonNull(size);
        Objects.requireNonNull(cornerRadius);
        Objects.requireNonNull(widget);
        return new Box(widget).withFixedSize(size).withCornerRadius(cornerRadius);
    }

    public record BorderSpec(Insets thickness, Widget fill) {
        public BorderSpec {
            Objects.requireNonNull(fill);
            Objects.requireNonNull(thickness);
        }
    }

    public record BoxShadow(Color color, Length blur, Length xOffset, Length yOffset, Length spread) {}
}