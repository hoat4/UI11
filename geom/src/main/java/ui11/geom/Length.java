package ui11.geom;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

import static java.lang.Math.abs;

// ez az osztály nem ide, hanem inkább ui-layout-model-be.
// de előtte át kéne gondolni, hogy mi lenne a sorsa hosszabb távon, mert jelenlegi formájában eléggé korlátolt
// (és a 3 mennyiség közül mindhárom olyan, ami nem egyértelmű, hogy mit jelent). 

public record Length(double em, double px, double rel) {

    public Length(double em, double px, double rel) {
        this.em = em;
        this.px = px;
        this.rel = rel;
        assert em == em && px == px && rel == rel;
    }

    public static Length zero() {
        return px(0);
    }

    public static Length relative(double ratioToParent) {
        return new Length(0, 0, ratioToParent);
    }

    public static Length percent(double percentageToParent) {
        return relative(percentageToParent / 100);
    }

    public static Length fillAvailableSpace() {
        return percent(100);
    }

    public static Length parentSize() {
        return relative(1);
    }

    // paraméternév azért nem "pixels" hanem px, mert akkor így IntelliJ nem rak inlay hintet,
    // mert ugyanaz mint a metódusnév
    public static Length px(double px) {
        return new Length(0, px, 0);
    }

    public static Length min() {
        return px(1);
    }

    public static Length em(double em) {
        return new Length(em, 0, 0);
    }

    @Override
    public String toString() {
        if (em == 0 && px == 0 && rel == 0)
            return "0px";
        if (em == 0 && px == 0)
            return rel * 100 + "%";
        if (em == 0 && rel == 0)
            return px + "px";
        if (px == 0 && rel == 0)
            return em + "em";
        StringBuilder sb = new StringBuilder("calc(");
        if (em != 0)
            sb.append(em).append("em + ");
        if (px != 0)
            sb.append(px).append("px");
        if (rel != 0) {
            if (px != 0)
                sb.append(" + ");
            sb.append(rel * 100).append("%");
        }
        return sb.append(')').toString();
    }

    public Length mul(double multiplier) {
        return new Length(em * multiplier, px * multiplier, rel * multiplier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Length length = (Length) o;
        return Double.compare(length.em, em) == 0 &&
                Double.compare(length.px, px) == 0 &&
                Double.compare(length.rel, rel) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(em, px, rel);
    }

    public boolean isRelative() {
        return rel != 0;
    }

    public boolean isOnlyPx() {
        return px != 0 && em == 0 && rel == 0;
    }

    public boolean isMixed() {
        return em != 0 && rel != 0 || px != 0 && rel != 0;
    }

    public static Length add(Length a, Length b) {
        return new Length(a.em + b.em, a.px + b.px, a.rel + b.rel);
    }

    public static Length nullsafeAdd(Length a, Length b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return new Length(a.em + b.em, a.px + b.px, a.rel + b.rel);
    }

    public static Length subtract(Length a, Length b) {
        return new Length(a.em - b.em, a.px - b.px, a.rel - b.rel);
    }

    public Length add(Length other) {
        return add(this, other);
    }

    public Length subtract(Length other) {
        return subtract(this, other);
    }

    public double asRelative() {
        if (em != 0 || px != 0)
            throw new UnsupportedOperationException();
        return rel;
    }

    public boolean isZero() {
        return rel < .000001 && em < .001 && px < .01;
    }

    public boolean equalsApproximately(Length other) {
        return abs(rel - other.rel) < .000001 && abs(em - other.em) < .001 && abs(px - other.px) < .01;
    }

    public static Length parse(String s) {
        if (s.equals("0"))
            return zero();
        if (s.endsWith("em"))
            return em(Double.parseDouble(s.substring(0, s.length() - 2)));
        if (s.endsWith("px"))
            return px(Double.parseDouble(s.substring(0, s.length() - 2)));
        if (s.endsWith("%"))
            return percent(Double.parseDouble(s.substring(0, s.length() - 1)));
        throw new IllegalArgumentException("unknown length format: " + s);
    }

    public double em() {
        return em;
    }

    public double px() {
        return px;
    }

    public double rel() {
        return rel;
    }

    public static Length interpolate(Length begin, Length end, double progress) {
        return end.mul(progress).add(begin.mul(1 - progress));
    }
}
