package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.Deque;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;

final class RefreshStack {

    private final Deque<Item> stack = new LinkedList<>();

    RefreshStack(@NonNull WidgetInstantiation root) {
        Objects.requireNonNull(root);
        stack.push(new Item(null, -100 /* érvénytelen érték */, root));
    }

    boolean isEmpty() {
        return stack.isEmpty();
    }

    boolean isRoot() {
        return stack.size() == 1;
    }

    @NonNull WidgetState<?> peekWidget() throws NoSuchElementException {
        return stack.element().widgetInstantiation.child();
    }

    @NonNull WidgetInstantiation peekWidgetInstantiation() {
        return stack.element().widgetInstantiation;
    }

    void push(@NonNull WidgetState<?> parent, int childIndex, @NonNull WidgetInstantiation child) {
        stack.push(new Item(parent, childIndex, child));
    }

    @NonNull Item pop() {
        return stack.pop();
    }

    public String stackToString() {
        if (stack.isEmpty())
            return "EMPTY";
        StringBuilder sb = new StringBuilder();
        for (Item item : stack) {
            Class<? extends Widget> widgetClass = item.widgetInstantiation.child().stateWidget.getClass();
            if (widgetClass != InheritedValueMerger.class &&
                    widgetClass != GlobalKey.GlobalKeyWidgetImpl.class &&
                    !PeerRequestor.class.isAssignableFrom(widgetClass) &&
                    !PeerRequestor.FinisherWidget.class.isAssignableFrom(widgetClass)) {
                sb.append("\n- ").append(widgetClass.getName());
                sb.append(" (needsRebuild=").append(item.needsRebuild).append(')');
            }
            item.widgetInstantiation.directIVs().forEach((type, val) -> {
                sb.append("\n   ").append(ReflectionUtil.simpleName(type)).append(" = ").append(val);
            });
        }
        return sb.toString();
    }

    void setDebugValuesOfCurrentWidget(boolean needsRebuild) {
        Item top = stack.element();
        top.needsRebuild = needsRebuild;
    }

    int depth() {
        return stack.size();
    }

    static final class Item {

        // az itteni parent helyett child.widgetState().parent nem jó,
        // mert WidgetState.parent lehet hogy még nincs beállítva

        public final @Nullable WidgetState<?> parent;
        public final int childIndex;
        public final @NonNull WidgetInstantiation widgetInstantiation;

        private Boolean needsRebuild;

        Item(@Nullable WidgetState<?> parent,
             int childIndex,
             @NonNull WidgetInstantiation widgetInstantiation) {
            this.parent = parent;
            this.childIndex = childIndex;
            this.widgetInstantiation = widgetInstantiation;
        }

        @Override
        public String toString() {
            // debuggerhez hasznos
            return widgetInstantiation.child().modelWidget.toString();
        }
    }
}
