package ui11.i18n;

import org.teavm.metaprogramming.CompileTime;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Inicializálás: először megadjuk a változókat {@code add*Var()} hívásokkal, majd {@linkplain #setPattern(String)}.
 * Ezután lehet hívni {@link #evaluate(Object...)}-et.
 */
@CompileTime
class RichTextPatternParser {

    private final Locale locale;
    private Node rootNode;
    private final List<Leaf> leaves = new ArrayList<>();

    public RichTextPatternParser(Locale locale) {
        this.locale = locale;
    }

    public void addInlineStringVar() {
        leaves.add(null);
    }

    public void addWidgetVar(String name) {
        leaves.add(new Leaf(leaves.size(), name, false));
    }

    public void addSpanDecoratorVar(String name) {
        leaves.add(new Leaf(leaves.size(), name + "Start", true));
        leaves.add(new Leaf(leaves.size(), name + "End", true));
    }

    public void setPattern(String pattern) {
        List<Node> nodes = new ArrayList<>();
        int mfBegin = 0;
        int braces = 0;
        int braceStart = -1;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '{') {
                if (braces == 0)
                    braceStart = i;
                braces++;
            } else if (pattern.charAt(i) == '}') {
                if (braces == 0)
                    throw new IllegalArgumentException("missing opening curly bracket");
                braces--;
                if (braces == 0) {
                    String content = pattern.substring(braceStart + 1, i);
                    if (!isNumber(content))
                        continue;
                    int num = Integer.parseInt(content);
                    if (num >= leaves.size()) {
                        // MessageFormat furán viselkedik, ha kevesebb a megadott argumentum értéket kap mint ahány
                        // szerepel a patternben: kiírja "{0}", "{1}", stb. szövegeket az helyükre, de ha pl.
                        // egy choice van, akkor levágja a subformat beállításáit, és akkor is csak az argumentum számát
                        // írja ki. Meg kéne kérdezni tőlük, hogy ez szándékos-e.

                        // ilyenkor miért nem inkább hibát jelzünk?
                        continue;
                    }

                    Leaf leaf = leaves.get(num);
                    if (leaf == null)
                        continue;
                    if (mfBegin != braceStart)
                        nodes.add(new MessageFormatNode(pattern.substring(mfBegin, braceStart)));
                    nodes.add(leaf);
                    mfBegin = i + 1;
                }
            }
        }
        if (mfBegin != pattern.length())
            nodes.add(new MessageFormatNode(pattern.substring(mfBegin)));

        rootNode = makeTree(nodes, null, -1);
    }

    private ContainerNode makeTree(List<Node> nodes, String name, int number) {
        List<Node> dst = new ArrayList<>();
        while (!nodes.isEmpty()) {
            Node node = nodes.remove(0);
            if (node instanceof Leaf) {
                Leaf n = (Leaf) node;
                switch (n.type) {
                    case INTERVAL_START:
                        dst.add(makeTree(nodes, n.name.substring(0, n.name.length() - 5), n.number));
                        continue;
                    case INTERVAL_END:
                        if (name == null)
                            throw new IllegalArgumentException("unmatched interval ending: " + n.name + ", " + n.number);
                        if (n.number == number + 1)
                            return new ContainerNode(name, dst);
                }
            }
            dst.add(node);
        }
        if (name != null)
            throw new IllegalArgumentException("unclosed interval: " + name);
        return new ContainerNode(null, dst);
    }

    private static boolean isNumber(String s) {
        if (s.isEmpty())
            return false;
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i)))
                return false;
        }
        return true;
    }

    public AnnotatedTextToken evaluate(Object... args) {
        return rootNode.evaluate(args);
    }

    private interface Node {

        AnnotatedTextToken evaluate(Object... args);
    }

    private class MessageFormatNode extends MessageFormat implements Node {

        public MessageFormatNode(String pattern) {
            super(pattern, locale);
        }

        @Override
        public AnnotatedTextToken evaluate(Object... args) {
            return new AnnotatedTextToken.StringToken(format(args));
        }
    }

    private static class Leaf implements Node {

        final int number;
        final AnnotatedTextToken value;
        final NodeType type;
        final String name;

        public Leaf(int number, String name, boolean isInterval) {
            this.number = number;
            this.name = name;
            this.value = new AnnotatedTextToken.SimpleToken(name);
            if (isInterval)
                type = name.endsWith("Start") ? NodeType.INTERVAL_START : NodeType.INTERVAL_END;
            else
                type = NodeType.NORMAL;
        }

        @Override
        public AnnotatedTextToken evaluate(Object... args) {
            return value;
        }
    }

    enum NodeType {
        NORMAL, INTERVAL_START, INTERVAL_END
    }

    private class ContainerNode implements Node {

        private final String name;
        private final List<Node> children;

        public ContainerNode(String name, List<Node> children) {
            this.name = name;
            this.children = children;
        }

        @Override
        public AnnotatedTextToken evaluate(Object... args) {
            List<AnnotatedTextToken> tokens = new ArrayList<>();
            for (Node node : children) {
                tokens.add(node.evaluate(args));
            }
            if (tokens.size() == 1 && name == null)
                return tokens.get(0);
            return new AnnotatedTextToken.ContainerToken(name, tokens);
        }
    }
}
