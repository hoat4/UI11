package ui11;

import ui11.observable.EventSource;
import ui11.observable.MutableObservable;
import ui11.observable.Scope;

import java.util.function.Supplier;

import static ui11.graphics.Empty.empty;

public class SlotOld extends Widget implements MutableObservable<Widget> {

    private final MutableObservable<Widget> content = MutableObservable.ofNullable();

    public SlotOld() {}

    public SlotOld(Widget content) {
        this.content.set(content);
    }

    @Override
    public Widget get() {
        return content.get();
    }

    @Override
    public EventSource<ChangeEvent<Widget>> changes() {
        return content.changes();
    }

    @Override
    public void set(Widget e) {
        content.set(e);
    }

    @Override
    public void bindTo(Supplier<Widget> valueSupplier, Scope scope) {
        content.bindTo(valueSupplier, scope);
    }

    @Override
    public Widget snoop() {
        return content.snoop();
    }

    @Override
    protected Widget build() {
        Widget e = get();
        return e == null ? empty() : e;
    }
}
