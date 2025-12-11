package ui11.geom;

import ui11.geom.Path.Close;
import ui11.geom.Path.LineTo;
import ui11.geom.Path.MoveTo;
import ui11.geom.Path.PathElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public record Rect(Vec2 origin, Size size) {

    public Rect {
        Objects.requireNonNull(origin);
        Objects.requireNonNull(size);
    }

    public Rect(double x, double y, double w, double h) {
        this(new Vec2(x, y), new Size(w, h));
    }

    public Rect(double x, double y, Size size) {
        this(new Vec2(x, y), size);
    }

    public static Rect of(Vec2 topLeft, Vec2 bottomRight) {
        return new Rect(topLeft, Size.of(topLeft, bottomRight));
    }

    public static Rect of(Size size) {
        return new Rect(Vec2.ZERO, size);
    }

    public static Rect of(Axis firstAxis,
                          double origin1, double origin2,
                          double size1, double size2) {
        return switch (firstAxis) {
            case HORIZONTAL -> new Rect(origin1, origin2, size1, size2);
            case VERTICAL -> new Rect(origin2, origin1, size2, size1);
        };
    }

    public static Rect ofTopRightBottomLeft(double top, double right, double bottom, double left) {
        return new Rect(left, top, right-left, bottom-top);
    }

    public static Rect of(Vec2... minimumContainedVec2s) {
        if (minimumContainedVec2s.length == 0)
            throw new IllegalArgumentException();

        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

        for (Vec2 p : minimumContainedVec2s) {
            minX = Math.min(p.x(), minX);
            minY = Math.min(p.y(), minY);
            maxX = Math.max(p.x(), maxX);
            maxY = Math.max(p.y(), maxY);
        }
        return of(new Vec2(minX, minY), new Vec2(maxX, maxY));
    }

    public Rect inset(double top, double right, double bottom, double left) {
        return new Rect(origin.plus(left, top), size.subtractOrZero(left + right, top + bottom));
    }

    public Rect outset(double top, double right, double bottom, double left) {
        return new Rect(origin.minus(left, top), size.add(left + right, top + bottom));
    }

    public boolean contains(Vec2 point) {
        // TODO right és bottomra <= lenne logikus, viszont úgy meg nem lehet két szomszédos de diszjunkt
        //      téglalapot létrehozni. de így meg egyrészt asszimetrikus, másrészt
        //      inkonzisztens contains(Rect)-tel. mi legyen?
        return point.x() >= origin.x() && point.y() >= origin.y()
                && point.x() < right() && point.y() < bottom();
    }

    public boolean contains(Rect other) {
        return other.left() >= left() && other.top() >= top()
                && other.right() <= right() && other.bottom() <= bottom();
    }

    public void iteratePath(Consumer<PathElement> consumer) {
        consumer.accept(new MoveTo(origin));
        consumer.accept(new LineTo(origin.plus(size.width(), 0)));
        consumer.accept(new LineTo(origin.plus(size.width(), size.height())));
        consumer.accept(new LineTo(origin.plus(0, size.height())));
        consumer.accept(new Close());
    }

    public Vec2 topLeft() {
        return origin;
    }

    public Vec2 topRight() {
        return origin.plus(size.width(), 0);
    }

    public Vec2 bottomLeft() {
        return origin.plus(0, size.height());
    }

    public Vec2 topCenter() {
        return origin.plus(size.width() / 2, 0);
    }

    public Vec2 bottomCenter() {
        return origin.plus(size.width() / 2, size.height());
    }

    public Vec2 leftCenter() {
        return origin.plus(0, size.height() / 2);
    }

    public Vec2 rightCenter() {
        return origin.plus(size.width(), size.height() / 2);
    }

    public Vec2 bottomRight() {
        return origin.plus(size.width(), size.height());
    }

    public List<Vec2> corners() {
        return List.of(
                topLeft(),
                topRight(),
                bottomRight(),
                bottomLeft()
        );
    }

    public Rect withTopLeft(Vec2 p) {
        return new Rect(p, new Size(size.width() + this.origin.x() - p.x(),
                size.height() + this.origin.y() - p.y()));
    }

    public double x() {
        return origin.x();
    }

    public double y() {
        return origin.y();
    }

    public double width() {
        return size.width();
    }

    public double height() {
        return size.height();
    }

    public double left() {
        return x();
    }

    public double right() {
        return x() + width();
    }

    public double top() {
        return y();
    }

    public double bottom() {
        return y() + height();
    }

    public Vec2 center() {
        return new Vec2(origin.x() + size.width() / 2, origin.y() + size.height() / 2);
    }

    @Nullable
    public Rect intersect(Rect newRect) {
        double left = Math.max(left(), newRect.left());
        double right = Math.min(right(), newRect.right());
        double top = Math.max(top(), newRect.top());
        double bottom = Math.min(bottom(), newRect.bottom());
        if (left > right || top > bottom)
            return null;
        else
            return new Rect(left, top, right - left, bottom - top);
    }

    @Override
    public String toString() {
        return "Rect(" + origin + " " + size + ")";
    }
}
