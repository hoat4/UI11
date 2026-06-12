package ui11.resolution;

import ui11.Widget;
import ui11.provide.UpValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

public class GlobalViewProviders implements WidgetResolver {

    private static final GlobalViewProviders INSTANCE = new GlobalViewProviders();
    private static final List<WidgetResolver> PROVIDERS;

    static {
        // kell cacheelni, mert folyton ServiceLoadert hívogatni lassú.
        // viszont TeaVM-ben nincs ServiceLoader.stream(), ezért muszáj iterátorral.
        // (TeaVM-ben egyébként valójában nem lenne lassú folyton ServiceLoaderen iterálni)
        List<WidgetResolver> providers = new ArrayList<>();
        for (WidgetResolver d : ServiceLoader.load(WidgetResolver.class))
            providers.add(d);
        PROVIDERS = List.copyOf(providers);
    }

    public static GlobalViewProviders instance() {
        return INSTANCE;
    }

    @Nullable
    @Override
    public Widget resolveOrNull(Widget widget, ResolutionContext resolutionContext) {
        for (WidgetResolver vp : PROVIDERS) {
            Widget e = vp.resolveOrNull(widget, resolutionContext);
            if (e != null)
                return e;
        }

        return null;
    }

    @Override
    @Nonnull
    public Widget resolveAdditional(@Nonnull Widget widget, @Nonnull Widget content) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(content);

        for (WidgetResolver vp : PROVIDERS.reversed()) {
            content = vp.resolveAdditional(widget, content);
            Objects.requireNonNull(content, "GVP rA");
        }

        return content;
    }
}
