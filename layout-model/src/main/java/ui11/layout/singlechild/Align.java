package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;

import java.util.Objects;

/**
 * Az ebben lévő függvények a megadott elemet berakják egy új konténerbe, ami képes a benne lévőt igazítva
 * megjeleníteni. A visszaadott érték az előbbi konténer.
 * <p>
 * Azok az igazítások, amik csak egyik tengely mentén igazítanak, azok a másik tengelyt békénhagyják. Azaz például
 * függőleges igazítás esetén a megadott elem szélességében ki fogja tölteni az új konténer teljes szélességét.
 */
public final class Align extends SubstitutedWidget {

    private final @NonNull Widget content;
    private final @Nullable Alignment alignment;
    private final boolean allowExpandOutside;

    public Align(@NonNull Widget content, Alignment alignment) {
        this(content, alignment, false);
    }

    public Align(@NonNull Widget content, @Nullable Alignment alignment, boolean allowExpandOutside) {
        this.content = Objects.requireNonNull(content);
        this.alignment = alignment;
        this.allowExpandOutside = allowExpandOutside;
    }

    @Override
    protected Align forSubstitution() {
        return new Align(
                withID("content", content),
                alignment,
                allowExpandOutside
        );
    }

    public @NonNull Widget content() {
        return content;
    }

    public @Nullable Alignment alignment() {
        return alignment;
    }

    public boolean allowExpandOutside() {
        return allowExpandOutside;
    }

    @Override
    public String toString() {
        return alignment + " " + content;
    }

    public static Align align(Alignment alignment, Widget element) {
        if (element == null)
            return null;
        return new Align(element, alignment);
    }

    public static Align left(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.LEFT);
    }

    public static Align left(Length distanceToLeft, Widget element) {
        if (element == null)
            return null;
        return left(Padding.atLeft(distanceToLeft, element));
    }

    public static Align hcenter(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.HCENTER);
    }

    public static Align right(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.RIGHT);
    }

    public static Align right(Length distanceFromRight, Widget element) {
        if (element == null)
            return null;
        return right(Padding.atRight(distanceFromRight, element));
    }

    public static Align top(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.TOP);
    }

    public static Align top(Length distanceFromTop, Widget element) {
        if (element == null)
            return null;
        return new Align(Padding.atTop(distanceFromTop, element), Alignment.TOP);
    }

    public static Align vcenter(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.VCENTER);
    }

    public static Align bottom(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.BOTTOM);
    }

    public static Align leftTop(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.LEFT_TOP);
    }

    public static Align leftTop(Length distanceFromLeftAndTop, Widget element) {
        if (element == null)
            return null;
        return leftTop(Padding.atLeftTop(distanceFromLeftAndTop, element));
    }

    public static Align leftTop(Length distanceFromLeft,
                                Length distanceFromTop, Widget element) {
        if (element == null)
            return null;
        return leftTop(Padding.atLeftTop(distanceFromLeft, distanceFromTop, element));
    }

    public static Align centerTop(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.CENTER_TOP);
    }

    public static Align rightTop(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.RIGHT_TOP);
    }

    public static Align rightTop(Length distanceFromRightAndTop, Widget element) {
        if (element == null)
            return null;
        return rightTop(Padding.atRightTop(distanceFromRightAndTop, element));
    }

    public static Align leftCenter(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.LEFT_CENTER);
    }

    public static Align center(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.CENTER);
    }

    public static Align rightCenter(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.RIGHT_CENTER);
    }

    public static Align leftBottom(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.LEFT_BOTTOM);
    }

    public static Align centerBottom(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.CENTER_BOTTOM);
    }

    public static Align centerBottom(Length distanceFromBottom, Widget element) {
        if (element == null)
            return null;
        return centerBottom(Padding.atBottom(distanceFromBottom, element));
    }

    public static Align rightBottom(Widget element) {
        if (element == null)
            return null;
        return new Align(element, Alignment.RIGHT_BOTTOM);
    }
}
