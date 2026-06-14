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

        private final StringBuilder sb = new StringBuilder();
        private int indent = 1;

        private RenderTreePrinter() {}

        public void prop(String name, Object value) {
            sb.append(' ').append(name).append("=").append(value);
        }

        public void child(String name, RenderNode value) {
            sb.append('\n');
            for (int i = 0; i < indent; i++)
                sb.append("    ");
            sb.append(name).append(": ").append(value);
            indent++;
            value.debugPrint(this);
            indent--;
        }

        public static String toString(RenderNode root) {
            RenderTreePrinter p =new RenderTreePrinter();
            p.sb.append(root.toString());
            root.debugPrint(p);
            p.sb.append('\n');
            return p.sb.toString();
        }
    }
}
