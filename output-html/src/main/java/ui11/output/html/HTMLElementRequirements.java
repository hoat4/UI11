package ui11.output.html;

import ui11.output.html.util.UTF8Writer;

public class HTMLElementRequirements {

    public HTMLElementType type;
    public ClassList cssClasses;
    public StyleProp styleProps; // TODO ezt használni is kéne

    public HTMLElementType typeOrDiv() {
        return type == null ? HTMLElementType.DIV : type;
    }

    public HTMLElementRequirements addCSSClass(String name) {
        cssClasses = new ClassList(name, cssClasses);
        return this;
    }

    public record ClassList(String className, ClassList next) {}

    public static abstract class StyleProp {

        public final StyleProp next;

        public StyleProp(StyleProp next) {
            this.next = next;
        }

        public abstract void write(UTF8Writer out);
    }
}
