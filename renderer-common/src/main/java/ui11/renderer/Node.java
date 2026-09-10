package ui11.renderer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.geom.Vec4;
import ui11.renderer.input.PickContext;

public abstract class Node {

    public abstract boolean pick(PickContext pickContext, Vec4 p);

    public abstract void debugPrint(RenderTreePrinter out);

    public static class RenderTreePrinter {

        private static final String INDENT = "    ";

        private final StringBuilder sb = new StringBuilder();
        private int indent = 1;

        public RenderTreePrinter() {
        }

        protected @NonNull String valueToString(@Nullable Object value) {
            return String.valueOf(value);
        }

        public void prop(String name, Object value) {
            String valueStr = valueToString(value);
            if (valueStr.contains("\n")) {
                sb.append('\n');
                for (int i = 0; i < indent; i++)
                    sb.append(INDENT);
                sb.append(name).append(" = ");
                sb.append(valueStr.replace("\n", "\n" +
                        INDENT.repeat(indent) + " ".repeat(name.length() + " = ".length())));
            } else {
                sb.append(' ').append(name).append("=").append(valueStr);
            }
        }

        public void child(String name, Node value) {
            sb.append('\n');
            for (int i = 0; i < indent; i++)
                sb.append(INDENT);
            sb.append(name).append(": ").append(value);
            indent++;
            value.debugPrint(this);
            indent--;
        }

        public String toString(Node root) {
            if (!sb.isEmpty())
                throw new IllegalStateException();

            sb.append(root.toString());
            root.debugPrint(this);
            sb.append('\n');
            return sb.toString();
        }
    }
}
