package ui11.layout.protocol;

import ui11.geom.Axis;
import ui11.geom.Size;

public record BoxConstraints(double minWidth, double minHeight,
                             double maxWidth, double maxHeight) {
    public BoxConstraints {
        if (minWidth < 0 || minHeight < 0 || maxWidth < 0 || maxHeight < 0 ||
                Double.isNaN(minWidth) || Double.isNaN(minHeight) ||
                Double.isNaN(maxWidth) || Double.isNaN(maxHeight) ||
                maxWidth < minWidth || maxHeight < minHeight)
            throw new IllegalArgumentException("invalid constraints: " +
                    minWidth + ", " + minHeight + ", " +
                    maxWidth + ", " + maxHeight);
    }

    public static BoxConstraints of(Axis firstAxis,
                                    double min1, double min2,
                                    double max1, double max2) {
        return switch (firstAxis) {
            case HORIZONTAL -> new BoxConstraints(min1, min2, max1, max2);
            case VERTICAL -> new BoxConstraints(min2, min1, max2, max1);
        };
    }

    public static BoxConstraints tight(Size size) {
        return new BoxConstraints(size.width(), size.height(),
                size.width(), size.height());
    }

    public boolean isSatisfiedBy(Size size) {
        return size.width() >= minWidth && size.height() >= minHeight &&
                size.width() <= maxWidth && size.height() <= maxHeight;
    }

    public BoxConstraints loosen() {
        return new BoxConstraints(0, 0, maxWidth, maxHeight);
    }

    public BoxConstraints loosenHorizontally() {
        return new BoxConstraints(0, minHeight, maxWidth, maxHeight);
    }

    public BoxConstraints loosenVertically() {
        return new BoxConstraints(minWidth, 0, maxWidth, maxHeight);
    }

    public Size min() {
        return new Size(minWidth, minHeight);
    }

    public double min(Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> minWidth;
            case VERTICAL -> minHeight;
        };
    }

    public double max(Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> maxWidth;
            case VERTICAL -> maxHeight;
        };
    }

    public BoxConstraints subtract(Size size) {
        return new BoxConstraints(
                Math.max(0, minWidth - size.width()),
                Math.max(0, minHeight - size.height()),
                Math.max(0, maxWidth - size.width()),
                Math.max(0, maxHeight - size.height())
        );
    }

    public Size clamp(Size size) {
        return new Size(
                Math.clamp(size.width(), minWidth, maxWidth),
                Math.clamp(size.height(), minHeight, maxHeight)
        );
    }

    public double clampWidth(double w) {
        return Math.clamp(w, minWidth, maxWidth);
    }

    public double clampHeight(double h) {
        return Math.clamp(h, minHeight, maxHeight);
    }

    public BoxConstraints withTightWidth(double w) {
        return new BoxConstraints(w, minHeight, w, maxHeight);
    }

    public BoxConstraints withTightHeight(double h) {
        return new BoxConstraints(minWidth, h, maxWidth, h);
    }
}
