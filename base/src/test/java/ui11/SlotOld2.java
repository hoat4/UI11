package ui11;

import ui11.observable.Observable;

// nem tudom, hogy ez hova kerüljön
public final class SlotOld2 extends Widget {

    private final Observable<? extends Widget> contentObservable;

    public SlotOld2(Observable<? extends Widget> contentObservable) {
        this.contentObservable = contentObservable;
    }

    @Override
    protected Widget build() {
        return contentObservable.get();
    }
}
