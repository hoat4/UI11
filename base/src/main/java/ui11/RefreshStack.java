package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

final class RefreshStack {

    private final Deque<Item> stack = new LinkedList<>();
    private final Map<Class<?>, @NonNull Deque<@Nullable Object>> ivs = new HashMap<>();

    RefreshStack(@NonNull WidgetInstantiation root) {
        Objects.requireNonNull(root);
        pushIVs(root);
        stack.push(new Item(null, -100 /* érvénytelen érték */, root));
    }

    boolean isEmpty() {
        boolean empty = stack.isEmpty();
        assert !empty || ivs.values().stream().allMatch(Deque::isEmpty);
        return empty;
    }

    @NonNull WidgetState<?> peekWidget() throws NoSuchElementException {
        return stack.element().child.widgetState();
    }

    @Nullable WidgetState<?> peekParent() {
        return stack.element().parent;
    }

    @NonNull WidgetInstantiation peekWidgetInstantiation() {
        return stack.element().child;
    }

    Object getIV(Class<?> type, Object ifNotExists) {
        Deque<@Nullable Object> ivStack = ivs.get(type);
        if (ivStack == null || ivStack.isEmpty())
            return ifNotExists;
        else
            return ivStack.element();
    }

    boolean ivsMatch(Map<Class<?>, @Nullable Object> b) {
        for (Map.Entry<Class<?>, Object> entry : b.entrySet()) {
            if (!Objects.equals(ivs.get(entry.getKey()), entry.getValue()))
                return false;
        }
        return true;
    }

    void push(@NonNull WidgetState<?> parent, int childIndex, @NonNull WidgetInstantiation child) {
        pushIVs(child);

        stack.push(new Item(parent, childIndex, child));
    }

    private void pushIVs(@NonNull WidgetInstantiation child) {
        child.directIVs().forEach((type, val) -> {
            ivs.computeIfAbsent(type, __ -> new ArrayDeque<>()).push(val);
        });
    }

    @NonNull Item pop() {
        Item item = stack.pop();
        item.child.directIVs().forEach((type, val) -> {
            Object popped = ivs.get(type).pop();
            assert popped == val;
        });
        return item;
    }

    /**
     * @return csak akkor {@code null}, ha a verem nem tartalmazza a megadott widgetet
     */
    @Nullable Map<Class<?>, Object> ivsUntil(@NonNull WidgetState<?> until) {
        if (peekWidget() == until)
            throw new IllegalArgumentException();

        Map<Class<?>, Object> ivs = new HashMap<>();
        for (Item item : stack) {
            if (item.child.widgetState() == until)
                return ivs;
            item.child.directIVs().forEach(ivs::putIfAbsent);
        }
        return null;
    }

    record Item(
            @Nullable WidgetState<?> parent,
            int childIndex,
            @NonNull WidgetInstantiation child) {

        // az itteni parent helyett child.widgetState().parent nem jó,
        // mert WidgetState.parent lehet hogy még nincs beállítva


        @Override
        public String toString() {
            // debuggerhez hasznos
            return child.widgetState().modelWidget.toString();
        }
    }
}
