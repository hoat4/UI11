package ui11.i18n;

import org.jspecify.annotations.NonNull;
import ui11.Widget;
import ui11.layout.multichild.flow.Flow;
import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Egy lokalizált szöveget és a formázási pontjaiba behelyettesítendő nem-szöveges elemeket tartalmazza.
 *
 * @param <E> a nem-szöveges elemek típusa. Ez a típusváltozó majd meg lesz szüntetve, és Widget lesz fixen helyette.
 */
public final class LocalizedRichText<E> extends Widget {

    public final AnnotatedTextToken rootToken;
    public final Map<String, ? extends ElementFunction<E>> elementFunctions;

    // csak editable-knél nemnull
    private final String resid;
    private final Observable<String> formatStringObservable;
    private final RichTextPatternParser parser;
    private final Object[] argArray;

    public LocalizedRichText(AnnotatedTextToken rootToken,
                             Map<String, ? extends ElementFunction<E>> elementFunctions) {

        this.rootToken = Objects.requireNonNull(rootToken);
        this.elementFunctions = elementFunctions;

        this.resid = null;
        this.formatStringObservable = null;
        this.parser = null;
        this.argArray = null;
    }

    LocalizedRichText(Observable<String> formatStringObservable,
                      String resid,
                      RichTextPatternParser parser,
                      Object[] argArray,
                      Map<String, ? extends ElementFunction<E>> elementFunctions) {
        this.formatStringObservable = formatStringObservable;
        this.parser = parser;
        this.argArray = argArray;
        this.elementFunctions = elementFunctions;
        this.rootToken = null;
        this.resid = resid;
    }

    /**
     * @throws ClassCastException if this is not a {@code LocalizedRichText<Widget>}
     */
    @Override
    protected Widget build() {
        Widget text = ((LocalizedRichText<Widget>) this).apply(Text::new, Flow::new);
        if (this.rootToken == null)
            return new ResidBubble(resid, text);
        else
            return text;
    }

    public E apply(Function<String, E> stringElementSupplier, Function<List<E>, E> combiner) {
        AnnotatedTextToken rootToken;
        if (this.rootToken == null) {
            parser.setPattern(formatStringObservable.get());
            rootToken = parser.evaluate(argArray);
        } else
            rootToken = this.rootToken;
        return applyImpl(rootToken, stringElementSupplier, combiner);
    }

    private E applyImpl(AnnotatedTextToken token,
                        Function<String, E> stringElementSupplier, Function<List<E>, E> combiner) {

        if (token instanceof AnnotatedTextToken.StringToken)
            return stringElementSupplier.apply(((AnnotatedTextToken.StringToken) token).value);

        if (token instanceof AnnotatedTextToken.SimpleToken) {
            String n = ((AnnotatedTextToken.SimpleToken) token).name;
            ElementFunction<E> elementFunction = findElementFunction(n);

            if (elementFunction instanceof LeafElementFunction)
                return ((LeafElementFunction<E>) elementFunction).get();
            else
                throw new RuntimeException("element function '" + n
                        + "' belongs to container element, not a leaf element: " + elementFunction);
        }

        AnnotatedTextToken.ContainerToken container = (AnnotatedTextToken.ContainerToken) token;
        List<E> children = new ArrayList<>();
        for (AnnotatedTextToken childToken : container.tokens)
            children.add(applyImpl(childToken, stringElementSupplier, combiner));

        E elem = combiner.apply(children);

        if (container.name != null) {
            ElementFunction<E> elementFunction = findElementFunction(container.name);

            if (elementFunction instanceof LocalizedRichText.Decorator)
                return ((Decorator<E>) elementFunction).apply(elem);
            else
                throw new RuntimeException("element function '" + container.name
                        + "' belongs to leaf element, not a container element: " + elementFunction);
        }

        return elem;
    }

    private @NonNull ElementFunction<E> findElementFunction(String n) {
        ElementFunction<E> elementFunction = elementFunctions.get(n);

        if (elementFunction == null) {
            String availableFunctions;
            if (elementFunctions.isEmpty())
                availableFunctions = "none are available";
            else
                availableFunctions = "available: " + String.join(", ", elementFunctions.keySet());
            throw new RuntimeException("no element function named '" + n + "' (" + availableFunctions + ")");
        }

        return elementFunction;
    }

    public interface ElementFunction<T> /* permits LeafElementFunction, IntervalElementFunction */ {
    }

    @FunctionalInterface
    public interface LeafElementFunction<T> extends ElementFunction<T>, Supplier<T> {
    }

    @FunctionalInterface
    public interface Decorator<T> extends ElementFunction<T>, UnaryOperator<T> {
    }
}
