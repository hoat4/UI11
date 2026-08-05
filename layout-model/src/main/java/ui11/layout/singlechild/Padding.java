package ui11.layout.singlechild;

import org.jspecify.annotations.NonNull;
import ui11.Slot2;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Length;
import ui11.layout.Insets;

import java.util.Objects;

import static ui11.geom.Length.zero;

/**
 * Körbeveszi a gyerekét egy üres területtel.
 */
// TODO meg kéne nézni hogy működnek-e ezzel a relatív méretek
public final class Padding extends SubstitutedWidget {

    private final Insets insets;
    private final Widget content;

    @Remember private Slot2 contentSlot;

    public Padding(@NonNull Insets insets, @NonNull Widget content) {
        this.insets = Objects.requireNonNull(insets);
        this.content = Objects.requireNonNull(content);
    }

    // TODO ha ez konstruktor és nem factory method, akkor legyen a top,right,bottom,left is konstruktor
    public Padding(Length topBottom, Length leftRight, Widget content) {
        this(new Insets(topBottom, leftRight), content);
    }

    @Override
    protected void initState() {
        contentSlot = new Slot2();
    }

    @Override
    protected Padding forSubstitution() {
        return new Padding(insets, contentSlot.with(content));
    }

    public static Padding atTopBottom(Length len, Widget w) {
        return new Padding(Insets.atTopBottom(len), w);
    }

    public static Padding atTopBottom(Length top, Length bottom, Widget w) {
        return new Padding(Insets.atTopBottom(top, bottom), w);
    }

    public static Padding atBottom(Length len, Widget w) {
        return new Padding(Insets.atBottom(len), w);
    }

    public static Padding atTop(Length len, Widget w) {
        return new Padding(Insets.atTop(len), w);
    }

    public static Padding atSide(Length len, Widget w) {
        return new Padding(Insets.atSide(len), w);
    }

    public static Padding atSide(Length leftPadding, Length rightPadding, Widget w) {
        return new Padding(Insets.atSide(leftPadding, rightPadding), w);
    }

    public static Padding atLeft(Length len, Widget w) {
        return new Padding(Insets.atLeft(len), w);
    }

    public static Padding atRight(Length len, Widget w) {
        return new Padding(Insets.atRight(len), w);
    }

    public static Padding atLeftTop(Length topAndLeft, Widget content) {
        return of(topAndLeft, zero(), zero(), topAndLeft, content);
    }

    public static Padding atLeftTop(Length top, Length left, Widget content) {
        return of(top, zero(), zero(), left, content);
    }

    public static Padding atRightTop(Length topAndRight, Widget content) {
        return of(topAndRight, topAndRight, zero(), zero(), content);
    }

    public static Padding allSides(Length len, Widget w) {
        return new Padding(Insets.all(len), w);
    }

    public static Padding of(Length topBottom, Length leftRight, Widget content) {
        return new Padding(new Insets(topBottom, leftRight), content);
    }

    public static Padding of(Length top, Length right, Length bottom, Length left, Widget content) {
        return new Padding(new Insets(top, right, bottom, left), content);
    }

    public @NonNull Insets insets() {
        return insets;
    }

    public @NonNull Widget content() {
        return content;
    }
}
