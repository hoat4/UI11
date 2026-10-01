package ui11.css;

import org.jspecify.annotations.Nullable;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.graphics.Empty;

import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// eredetileg classNames List<String> helyett String volt, mert 1 classnév esetén ez a
// kompaktabb reprezentáció, 2 classnév esetén pedig ugyanannyi memóriát igényelnek, ha
// Listnek List12-t használunk. Sőt, ha ListN-t vagy ArrayListet, akkor valszeg 3 classnév
// esetén is valszeg kevésbé tömör, mint a List nélküli láncolt ábrázolás.
// Illetve így a konstruktorban nem kell foglalkozni a listák egymásba olvasztásával.

// De ez nem jó, mert ha eltérő számú CSS class nemnull a CSSClassTag.cssClass-nak megadottak közül,
// akkor eltérő mélységű widget részfát hozott létre, és elveszik a state ha változik hogy hány class nemnull.
// Ezért át lett írva List<String>-re.
// Alternatíva lenne, ha egy explicit slotot használna a CSSClassTag.cssClass, de attól nagyobb lenne a fa mélység.

/**
 * @see WrapWithCSSClassTag
 */
public final class CSSClassTag extends SubstitutedWidget {

    private final List<String> classNames;
    private final Widget content;

    private CSSClassTag(List<String> classNames, Widget content) {
        Objects.requireNonNull(content);
        Objects.requireNonNull(classNames);
        // lehetne azt is ellenőrizni hogy nem üres string-e
        this.classNames = classNames;
        this.content = content;
    }

    @Override
    protected CSSClassTag forSubstitution() {
        return new CSSClassTag(
                classNames,
                withID("content", content)
        );
    }

    public Collection<String> classNames() {
        return classNames;
    }

    public Widget content() {
        return content;
    }

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }

    public static Widget cssClass(@Nullable String className, @Nullable Widget widget) {
        if (widget == null)
            return null;
        return new CSSClassTag(className == null ? List.of() : List.of(className), widget);
    }

    // TODO duplicateekért szóljunk?
    public static Widget cssClass(@Nullable String className1, @Nullable String className2,
                                  @Nullable Widget widget) {
        if (widget == null)
            return null;
        return new CSSClassTag(
                className1 == null ?
                        className2 == null ? List.of() : List.of(className2) :
                        className2 == null ? List.of(className1) : List.of(className1, className2),
                widget
        );
    }

    public static Widget cssClass(@Nullable String className1, @Nullable String className2,
                                  @Nullable String className3, @Nullable Widget widget) {
        if (widget == null)
            return null;
        return new CSSClassTag(
                className1 == null ?
                        className2 == null ?
                                className3 == null ? List.of() : List.of(className3) :
                                className3 == null ? List.of(className2) : List.of(className2, className3) :
                        className2 == null ?
                                className3 == null ? List.of(className1) : List.of(className1, className3) :
                                className3 == null ? List.of(className1, className2) : List.of(className1, className2, className3),
                widget
        );
    }

    public static Widget cssClass(@NonNull List<@Nullable String> classNames, @Nullable Widget widget) {
        if (widget == null)
            return null;
        return new CSSClassTag(classNames.stream().
                filter(Objects::nonNull).collect(Collectors.toUnmodifiableList()),
                widget);
    }

    public static Widget cssGraphic(@Nullable String className) {
        return cssClass(className, Empty.empty());
    }

    public static Widget cssGraphic(@Nullable String @NonNull ... classNames) {
        Widget w = Empty.empty();
        return cssClass(Arrays.asList(classNames), w);
    }
}
