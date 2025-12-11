package ui11.css;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

public class StylesheetRef {

    public final URL url; // ezt majd ki kéne szedni a generált JS-ből
    private final Map<Class<? extends StylesheetPreparer<?>>, Object> preparedData;

    StylesheetRef(URL url, Map<Class<? extends StylesheetPreparer<?>>, Object> preparedData) {
        this.url = url;
        this.preparedData = Map.copyOf(preparedData);
    }

    @SuppressWarnings("unchecked")
    public static StylesheetRef of(URL url) {
        Objects.requireNonNull(url, "url");
        Map<Class<? extends StylesheetPreparer<?>>, Object> preparedData = new HashMap<>();
        for (StylesheetPreparer<?> s : ServiceLoader.load(StylesheetPreparer.class)) {
            preparedData.put((Class<? extends StylesheetPreparer<?>>) s.getClass(), s.prepare(url));
        }
        return new StylesheetRef(url, preparedData);
    }

    @SuppressWarnings("unchecked")
    public <R> R preparedData(Class<? extends StylesheetPreparer<R>> preparerClass) {
        if (!preparedData.containsKey(preparerClass))
            throw new IllegalArgumentException();
        else
            return (R) preparedData.get(preparerClass);
    }

    /* TODO
    public WidgetResolver asViewProvider(WidgetResolver prev) {
        WidgetResolver vp = (WidgetResolver) prev.createView(this, WidgetResolver.class);
        return WidgetResolver.composite(prev, vp);
    }
     */
}
