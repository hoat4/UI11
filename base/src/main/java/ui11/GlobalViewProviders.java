package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

class GlobalViewProviders implements WidgetResolver {

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

    @Override
    public @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
        for (WidgetResolver vp : PROVIDERS) {
            Widget e = vp.resolveOrNull(widget, peerCreationRequest);
            if (e != null)
                return e;
        }

        return null;
    }

    @Override
    public @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content) {
        Objects.requireNonNull(widget);
        Objects.requireNonNull(content);

        for (WidgetResolver vp : PROVIDERS.reversed()) {
            content = vp.resolveAdditional(widget, content);
            Objects.requireNonNull(content, "GVP rA");
        }

        return content;
    }
}
