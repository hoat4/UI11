package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.reflectutil.ReflectionUtil;

import java.util.*;

final class RefreshStack {

    private final Deque<Item> stack = new LinkedList<>();
    private final Map<Class<?>, Deque<IVValueWrapper>> ivs = new HashMap<>();

    RefreshStack(@NonNull WidgetInstantiation root) {
        Objects.requireNonNull(root);
        stack.push(new Item(null, -100 /* érvénytelen érték */, root));
    }

    boolean isEmpty() {
        boolean empty = stack.isEmpty();
        assert !empty || ivs.values().stream().allMatch(Deque::isEmpty);
        return empty;
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

    IVValueWrapper getIV(Class<?> type) {
        Deque<IVValueWrapper> ivStack = ivs.get(type);
        if (ivStack == null || ivStack.isEmpty())
            return null;
        else {
            IVValueWrapper result = ivStack.element();
            assert result.value == null || type.isInstance(result.value);
            return result;
        }
    }

    boolean ivsMatch(Map<Class<?>, @Nullable Object> b) {
        for (Map.Entry<Class<?>, Object> entry : b.entrySet()) {
            IVValueWrapper actual = getIV(entry.getKey());
            // TODO ez a null jó?
            if (!Objects.equals(actual == null ? null : actual.valueForComparison, entry.getValue()))
                return false;
        }
        return true;
    }

    void push(@NonNull WidgetState<?> parent, int childIndex, @NonNull WidgetInstantiation child) {
        stack.push(new Item(parent, childIndex, child));
    }

    void pushIVs(@NonNull WidgetState<?> expectedW, @NonNull Map<Class<?>, IVValueWrapper> ivs) {
        Item item = stack.element();
        assert item.widgetInstantiation.child() == expectedW;
        assert item.pushedIVs == null;
        item.pushedIVs = ivs;
        ivs.forEach((type, val) -> {
            this.ivs.computeIfAbsent(type, __ -> new ArrayDeque<>()).push(val);
        });
    }

    @NonNull Item pop() {
        Item item = stack.pop();
        item.pushedIVs.forEach((type, val) -> {
            IVValueWrapper popped = ivs.get(type).pop();
            assert popped == val;
        });
        return item;
    }

    public String toDebugString() {
        if (stack.isEmpty())
            return "EMPTY";
        StringBuilder sb = new StringBuilder();
        for (Item item : stack) {
            sb.append("\n- ").append(item.widgetInstantiation.child().stateWidget.getClass().getName());
            sb.append(" (needsRebuild=").append(item.needsRebuild).append(')');
            item.widgetInstantiation.directIVs().forEach((type, val) -> {
                sb.append("\n    ").append(ReflectionUtil.simpleName(type)).append(" = ").append(val);
            });
        }
        return sb.toString();
    }

    void setDebugValuesOfCurrentWidget(boolean needsRebuild) {
        Item top = stack.element();
        top.needsRebuild = needsRebuild;
    }

    static final class Item {

        // az itteni parent helyett child.widgetState().parent nem jó,
        // mert WidgetState.parent lehet hogy még nincs beállítva

        public final @Nullable WidgetState<?> parent;
        public final int childIndex;
        public final @NonNull WidgetInstantiation widgetInstantiation;

        private Boolean needsRebuild;

        private Map<Class<?>, IVValueWrapper> pushedIVs;

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

    static class IVValueWrapper {

        final @Nullable Object value;
        final @Nullable Object valueForComparison;

        /**
         * ha {@code null}, az azért van nem kell rá feliratkozni, mert mergeölt érték ami
         * ancestorból jött és ott már feliratkoztunk rá
         */
        final @Nullable WidgetInstantiation origin;
        final boolean isFromDescendant;

        IVValueWrapper(@Nullable Object value, @Nullable Object valueForComparison,
                       @Nullable WidgetInstantiation origin, boolean isFromDescendant) {
            this.value = value;
            this.valueForComparison = valueForComparison;
            this.origin = origin;
            this.isFromDescendant = isFromDescendant;
        }
    }
}
