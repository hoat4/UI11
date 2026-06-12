package ui11.geom;

public record Size(double width, double height) {

    public static final Size ZERO = new Size(0, 0);

    public Size {
        // NaN miatt < nem ugyananz mint !(>=)
        if (!(width >= 0 && height >= 0 && Double.isFinite(width) && Double.isFinite(height)))
            throw new IllegalArgumentException("width and height must be positive and finite: " +
                    width + ", " + height);
    }

    public double length(Axis axis) {
        return switch (axis) {
            case HORIZONTAL -> width;
            case VERTICAL -> height;
        };
    }

    public Size add(Size other) {
        return new Size(width + other.width, height + other.height);
    }

    public Size add(double w, double h) {
        return new Size(width + w, height + h);
    }

    public Size mul(double multiplier) {
        if (!(multiplier >= 0))
            throw new IllegalArgumentException("not a non-negative multiplier: " + multiplier);
        return new Size(width * multiplier, height * multiplier);
    }

    public Size div(double divisor) {
        return new Size(width / divisor, height / divisor);
    }

    public Size subtract(double dw, double dh) {
        return new Size(width - dw, height - dh);
    }

    public Size subtractOrZero(double dw, double dh) {
        return new Size(Math.max(0, width - dw), Math.max(0, height - dh));
    }

    public boolean isNonNegative() {
        return width >= 0 && height >= 0;
    }

    public Size withWidth(double w) {
        return new Size(w, height);
    }

    public Size withHeight(double h) {
        return new Size(width, h);
    }

    public static Size of(Vec2 widthAndHeight) {
        return new Size(widthAndHeight.x(), widthAndHeight.y());
    }

    public static Size of(Vec2 topLeft, Vec2 bottomRight) {
        return new Size(bottomRight.x() - topLeft.x(), bottomRight.y() - topLeft.y());
    }

    public static Size of(Axis firstAxis, double l1, double l2) {
        return switch (firstAxis) {
            case HORIZONTAL -> new Size(l1, l2);
            case VERTICAL -> new Size(l2, l1);
        };
    }

    public Vec2 middle() {
        return new Vec2(width / 2, height / 2);
    }

    @Override
    public String toString() {
        // IntelliJ Run dialógus nem jól jeleníti meg × karaktert
        return "(" + width + " x " + height + ")";
    }

    public boolean isLargerThan(Size size) {
        return width > size.width || height > size.height;
    }

    public static Size min(Size a, Size b) {
        return new Size(Math.min(a.width, b.width), Math.min(a.height, b.height));
    }

    public static Size max(Size a, Size b) {
        return new Size(Math.max(a.width, b.width), Math.max(a.height, b.height));
    }

    public double shorter() {
        return Math.min(width, height);
    }

    public Size round() {
        return new Size(Math.round(width), Math.round(height));
    }

    public Size ceil() {
        return new Size(Math.ceil(width), Math.ceil(height));
    }

    public Vec2 asVec2() {
        return new Vec2(width, height);
    }
}
