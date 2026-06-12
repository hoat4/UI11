package ui11.i18n;

import java.util.List;
import java.util.stream.Collectors;

public sealed abstract class AnnotatedTextToken {

    protected AnnotatedTextToken() {
    }

    /**
     * nem grafikus környezetben is használható változatát adja vissza ennek a fának (tehát a beágyazott widgetek
     * eltűnnek, és dekorációk sem lesznek alkalmazva)
     */
    @Override
    public abstract String toString();

    public static final class StringToken extends AnnotatedTextToken {

        public final String value;

        public StringToken(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static final class SimpleToken extends AnnotatedTextToken {

        public final String name;

        public SimpleToken(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "";
        }
    }

    public static final class ContainerToken extends AnnotatedTextToken {

        public final String name;
        public final List<AnnotatedTextToken> tokens;

        public ContainerToken(String name, List<AnnotatedTextToken> tokens) {
            this.name = name;
            this.tokens = tokens;
        }

        @Override
        public String toString() {
            // TeaVM-ben nincs StringJoiner
            return tokens.stream().map(AnnotatedTextToken::toString).collect(Collectors.joining());
        }
    }
}
