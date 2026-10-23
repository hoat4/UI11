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

    RefreshStack(@NonNull Edge root) {
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

    @NonNull Edge peekWidgetInstantiation() {
        return stack.element().widgetInstantiation;
    }

    void push(@NonNull WidgetState<?> parent, int childIndex, @NonNull Edge child) {
        stack.push(new Item(parent, childIndex, child));
    }

    @NonNull Item pop() {
        return stack.pop();
    }

    public String stackToString(boolean showInternal) {
        if (stack.isEmpty())
            return "EMPTY";
        StringBuilder sb = new StringBuilder();
        int i = depth();
        for (Item item : stack) {
            Class<? extends Widget> widgetClass = item.widgetInstantiation.child().stateWidget.getClass();
            --i;
            if (shouldIncludeInPrint(widgetClass, showInternal)) {
                sb.append("\n").append(i).append(". ").append(widgetClass.getName());
                sb.append(" (needsRebuild=").append(item.needsRebuild).append(')');
            }
            item.widgetInstantiation.directIVs().forEach((type, val) -> {
                sb.append("\n   ").append(ReflectionUtil.simpleName(type)).append(" = ").append(val);
            });
        }
        return sb.toString();
    }

    private static boolean shouldIncludeInPrint(Class<? extends Widget> widgetClass, boolean showInternal) {
        if (showInternal)
            return true;
        return widgetClass != InheritedValueMerger.class &&
            widgetClass != GlobalKey.GlobalKeyWidgetImpl.class &&
            !PeerRequestor.class.isAssignableFrom(widgetClass) &&
            !PeerRequestor.FinisherWidget.class.isAssignableFrom(widgetClass);
    }

    void setDebugValuesOfCurrentWidget(boolean needsRebuild) {
        Item top = stack.element();
        top.needsRebuild = needsRebuild;
    }

    int depth() {
        return stack.size();
    }

    boolean containsErrorWidget() {
        return stack.stream().
                anyMatch(item -> item.widgetInstantiation.child().stateWidget instanceof ErrorWidget);
    }

    static final class Item {

        // az itteni parent helyett child.widgetState().parent nem jó,
        // mert WidgetState.parent lehet hogy még nincs beállítva

        public final @Nullable WidgetState<?> parent;
        public final int childIndex;
        public final @NonNull Edge widgetInstantiation;

        private Boolean needsRebuild;

        Item(@Nullable WidgetState<?> parent,
             int childIndex,
             @NonNull Edge widgetInstantiation) {
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
