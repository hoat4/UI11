package ui11.resolution;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.Widget;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
public interface WidgetResolver {

    /**
     * @return ha null, az azt jelenti hogy nem tudott vele mit kezdeni, nem azt hogy ürességre decomposeolta
     */
    @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest);

    // TODO mit csináljon a hívó kód, ha ez nullt ad vissza?
    default @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content) {
        return content;
    }

    static WidgetResolver composite(@NonNull WidgetResolver defaults, @NonNull WidgetResolver override) {
        Objects.requireNonNull(defaults, "WRc d");
        Objects.requireNonNull(override, "WRc o");

        return new WidgetResolver() {

            @Override
            public @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull PeerCreationRequest<?> peerCreationRequest) {
                Widget e2 = override.resolveOrNull(widget, peerCreationRequest);
                if (e2 != null)
                    return e2;
                else
                    return defaults.resolveOrNull(widget, peerCreationRequest);
            }

            @Override
            public @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content) {
                Objects.requireNonNull(widget);
                Objects.requireNonNull(content);

                content = defaults.resolveAdditional(widget, content);
                Objects.requireNonNull(content, "WRc rA d");
                content = override.resolveAdditional(widget, content);
                Objects.requireNonNull(content, "WRc rA o");
                return content;
            }
        };
    }
}
