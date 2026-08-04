package ui11;

import ui11.observable.MutableObservable;

// TODO név? IdentityPreservingSlot jutott eszembe először, de az nem érthető
public final class Slot2 {

    private final MutableObservable<Widget> content = MutableObservable.ofNullable();
    private final SlotWidget w;

    WidgetState<SlotWidget> widgetState;

    public Slot2() {
        this.w = new SlotWidget(this);
    }

    // itt azért nincs return typeon nullability megadva, mert
    // akkor nemnull, ha param type is nemnull
    public Widget with(Widget content) {
        if (content == null)
            return null;

        this.content.set(content);
        return w;
    }

    // WidgetTree.findOrCreateWidgetState-ben special case-elve van ez a widget, hogyha SlotWidgetet
    // talál, akkor ignorálja a previous WidgetInstantiationt és a KeyWrappereket is
    static class SlotWidget extends Widget {

        final Slot2 slot;

        private SlotWidget(Slot2 slot) {
            this.slot = slot;
        }

        @Override
        protected Widget build() {
            return slot.content.get();
        }
    }
}
