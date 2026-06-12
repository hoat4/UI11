package ui11.platform.awt.j2d.inputtree;

import ui11.geom.Vec2;

import java.util.ArrayList;
import java.util.List;

public abstract class InputNode {

    public abstract boolean pick(PickContext pickContext, Vec2 p);

    public static class PickContext {

        private final List<PickStackItem> stack = new ArrayList<>();
        private List<PickStackItem> result;

        public boolean addResult() {
            if (result != null)
                throw new IllegalStateException();
            result = List.copyOf(stack);
            return true;
        }

        public void push(ListenerInputNode listener, Vec2 p) {
            stack.add(new PickStackItem(listener, p));
        }

        public void pop(ListenerInputNode listener) {
            PickStackItem last = stack.removeLast();
            if (last.n != listener)
                throw new IllegalStateException("listener mismatch: expected " + listener + ", actual " + last);
        }

        public List<PickStackItem> result() {
            return result;
        }

        public record PickStackItem(ListenerInputNode n, Vec2 p) {
        }
    }
}
