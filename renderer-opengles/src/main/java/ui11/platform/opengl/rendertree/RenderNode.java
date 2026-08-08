package ui11.platform.opengl.rendertree;

import ui11.geom.Mat4;
import ui11.platform.opengl.renderer.displaylist.DisplayList;

public abstract class RenderNode {

    public abstract void addToDisplayList(Mat4 transform, DisplayList displayList);

    public abstract void debugPrint(RenderTreePrinter out);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
    }

    public static class RenderTreePrinter {

        private static final String INDENT = "    ";

        private final StringBuilder sb = new StringBuilder();
        private int indent = 1;

        private RenderTreePrinter() {
        }

        public void prop(String name, Object value) {
            String valueStr = String.valueOf(value);
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

        public void child(String name, RenderNode value) {
            sb.append('\n');
            for (int i = 0; i < indent; i++)
                sb.append(INDENT);
            sb.append(name).append(": ").append(value);
            indent++;
            value.debugPrint(this);
            indent--;
        }

        public static String toString(RenderNode root) {
            RenderTreePrinter p = new RenderTreePrinter();
            p.sb.append(root.toString());
            root.debugPrint(p);
            p.sb.append('\n');
            return p.sb.toString();
        }
    }
}
