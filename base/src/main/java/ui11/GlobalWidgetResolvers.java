package ui11;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

class GlobalWidgetResolvers {

    private static final WidgetResolver INSTANCE;

    static {
        // kell cacheelni, mert folyton ServiceLoadert hívogatni lassú.
        // viszont TeaVM-ben nincs ServiceLoader.stream(), ezért muszáj iterátorral.
        // (TeaVM-ben egyébként valójában nem lenne lassú folyton ServiceLoaderen iterálni)
        // TODO mégsem jó a cacheelés, mert thread context class loadert néz.
        //      akkor widgettree-nként kéne?
        List<WidgetResolver> providers = new ArrayList<>();
        for (WidgetResolver d : ServiceLoader.load(WidgetResolver.class))
            providers.add(d);
        // TODO sorrend?
        // TODO mi történik ha WidgetResolver konstruktor exceptiont dob?
        //      dokumentálni kéne ezt a javadocba
        INSTANCE = new WidgetResolver.CompositeWidgetResolver(providers);
    }

    public static WidgetResolver instance() {
        return INSTANCE;
    }
}
