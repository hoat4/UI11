package ui11.layout;

import ui11.Widget;
import ui11.observable.Observable;

// nem tudom, hogy ez hova kerüljön
public final class Slot extends Widget {

    private final Observable<? extends Widget> contentObservable;

    public Slot(Observable<? extends Widget> contentObservable) {
        this.contentObservable = contentObservable;
    }

    @Override
    protected Widget build() {
        return contentObservable.get();
    }
}
