package ui11.i18n;

import ui11.observable.MutableObservable;
import ui11.observable.Observable;
import ui11.observable.ObservableMap;
import ui11.observable.ObserverHolder;

import java.util.HashMap;
import java.util.Map;

public class LocalizableTextEditingContext {

    /**
     * ebbe ne írjunk közvetlenül, csak {@link #stringFor(String, String)}-on keresztül
     */
    public final ObservableMap<String, MutableObservable<String>> localizationResources = new ObservableMap<>();
    public final MutableObservable<Boolean> isEditing = MutableObservable.withInitial(false);

    public MutableObservable<String> stringFor(String name, String initialValue) {
        // azért kell a withoutObserver, hogy ne frissüljünk állandóan, amint valaki
        // egy újabb entry-t bejegyez ctx.localizationResources-ba (
        return ObserverHolder.withoutObserver(() -> {
            MutableObservable<String> result = localizationResources.get(name);
            if (result == null) {
                result = MutableObservable.withInitial(initialValue);
                localizationResources.put(name, result);
            }
            return result;
        });
    }
}
