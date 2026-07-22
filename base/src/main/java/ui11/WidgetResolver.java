package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Tartalmat biztosít az általa ismert típusú elemekhez. Megvalósítja egyrészt minden renderer, másrészt a jövőben
 * megvalósíthatják majd "look and feel"/"theme"-szerű modulok.
 */
public abstract class WidgetResolver {

    private Class<? extends SubstitutedWidget> supportedTargetType;

    protected abstract Class<? extends SubstitutedWidget> supportedTargetType();

    /**
     * @return ha null, az azt jelenti hogy nem tudott vele mit kezdeni, nem azt hogy ürességre decomposeolta
     */
    protected abstract @Nullable Widget resolveOrNull(@NonNull Widget widget);

    // TODO mit csináljon a hívó kód, ha ez nullt ad vissza?
    protected @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content) {
        return content;
    }

    // kavarodást okozna, ha supportedTargetType() többször meghívva mást ad vissza, ezért inkább eltároljuk az
    // elsőre visszaadott eredményt
    private Class<? extends SubstitutedWidget> supportedTargetTypeInternal() {
        if (supportedTargetType == null)
            // TODO multithread miatt lehet hogy kétszer lesz meghívva
            supportedTargetType = supportedTargetType();

        return supportedTargetType;
    }

    @Nullable Widget resolveOrNull(@NonNull Widget widget,
                                   ResolutionRequestCollection peerCreationRequestCollection) {
        if (peerCreationRequestCollection.requests.keySet().stream().anyMatch(peerType->
                supportedTargetTypeInternal().isAssignableFrom(peerType)))
            return resolveOrNull(widget);
        else
            return null;
    }

    @Nullable Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content,
                                       ResolutionRequestCollection peerCreationRequestCollection) {
        if (peerCreationRequestCollection.requests.keySet().stream().anyMatch(peerType->
                supportedTargetTypeInternal().isAssignableFrom(peerType)))
            return resolveAdditional(widget, content);
        else
            return null;
    }

    public static WidgetResolver composite(@NonNull WidgetResolver defaults, @NonNull WidgetResolver override) {
        Objects.requireNonNull(defaults, "WRc d");
        Objects.requireNonNull(override, "WRc o");
        return new CompositeWidgetResolver(List.of(override, defaults));
    }

    static class CompositeWidgetResolver extends WidgetResolver {

        private final List<? extends WidgetResolver> resolvers;

        public CompositeWidgetResolver(List<? extends WidgetResolver> resolvers) {
            this.resolvers = resolvers;
        }

        @Override
        @Nullable Widget resolveOrNull(@NonNull Widget widget, @NonNull ResolutionRequestCollection peerCreationRequest) {
            Objects.requireNonNull(widget);
            Objects.requireNonNull(peerCreationRequest);

            for (WidgetResolver resolver : resolvers) {
                Widget e2 = resolver.resolveOrNull(widget, peerCreationRequest);
                if (e2 != null)
                    return e2;
            }
            return null;
        }

        @Override
        @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content,
                                          @NonNull ResolutionRequestCollection peerCreationRequest) {
            Objects.requireNonNull(widget);
            Objects.requireNonNull(content);
            Objects.requireNonNull(peerCreationRequest);

            for (WidgetResolver resolver : resolvers.reversed()) {
                content = resolver.resolveAdditional(widget, content, peerCreationRequest);
                Objects.requireNonNull(content, "WRc rA " + resolver);
            }
            return content;
        }

        @Override
        protected Class<? extends SubstitutedWidget> supportedTargetType() {
            throw new UnsupportedOperationException();
        }

        @Override
        protected @Nullable Widget resolveOrNull(@NonNull Widget widget) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected @NonNull Widget resolveAdditional(@NonNull SubstitutedWidget widget, @NonNull Widget content) {
            throw new UnsupportedOperationException();
        }
    }
}
