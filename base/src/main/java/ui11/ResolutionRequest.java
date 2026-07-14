package ui11;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.PeerCreationRequest.ResolutionResult;
import ui11.observable.MutableObservable;
import ui11.provide.Provider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class ResolutionRequest<P extends SubstitutedWidget> {

    final @Nullable WidgetState<?> container;
    final @NonNull Widget widget;
    final @NonNull PeerCreationRequest<P> requestData;
    final @NonNull MutableObservable<@Nullable ResolutionResult<P>> result =
            MutableObservable.ofNullable();

    @Nullable WidgetState<?> finisherWidget;

    // a következő kettőt együtt állítjuk be
    private WidgetState<?> widgetState;
    private Map<Class<?>, Object> directIVsAtPrimaryLocation;

    // a következő kettőt együtt állítjuk be
    private long refreshIDOfIVsFromSecondaryLocation;
    private Map<Class<?>, Object> ivsFromSecondaryLocation;

    /**
     * létrehozás után állítsuk be {@link #finisherWidget}-et ha van (rooton kívül mindig van),
     * illetve a {@linkplain #setWidgetInstantiation létrehozott WidgetStateet}
     *
     * @param container root esetén null
     */
    public ResolutionRequest(
            @Nullable WidgetState<?> container,
            @NonNull WidgetTree tree,
            @NonNull PeerCreationRequest<P> requestData,
            @NonNull Widget widget
    ) {
        this.container = container;
        this.requestData = Objects.requireNonNull(requestData);
        this.widget = widget;
    }

    void setWidgetInstantiation(WidgetInstantiation widgetInstantiation) {
        assert this.widgetState == null && this.directIVsAtPrimaryLocation == null;
        assert widgetInstantiation.shouldSetParent();
        this.widgetState = widgetInstantiation.widgetState();
        this.directIVsAtPrimaryLocation = widgetInstantiation.directIVs();
    }

    void setResultUnchecked(@NonNull SubstitutedWidget peer,
                            @NonNull List<? extends ParentDataWidget> parentDataWidgets) {
        Objects.requireNonNull(peer);
        Objects.requireNonNull(parentDataWidgets);

        Map<Class<? extends ParentDataWidget>, ParentDataWidget> parentDataMap = new HashMap<>();
        for (Class<? extends ParentDataWidget> type : requestData.auxiliaryTypes) {
            for (ParentDataWidget w : parentDataWidgets)
                if (type.isInstance(w)) {
                    parentDataMap.put(type, type.cast(w));
                    break;
                }
        }

        @SuppressWarnings("unchecked") final P castedPeer = (P) peer;
        this.result.set(new ResolutionResult<>(this, castedPeer, Map.copyOf(parentDataMap)));
    }

    ResolutionResult<P> resultOrFail() {
        ResolutionResult<P> value = result.get();
        if (value == null)
            throw new RuntimeException("resolution failed");
        else
            return value;
    }

    Widget primaryWrapper() {
        Widget w = widget;

        if (ivsFromSecondaryLocation != null)
            for (Map.Entry<Class<?>, Object> entry : ivsFromSecondaryLocation.entrySet())
                if (entry.getKey() != ResolutionRequestCollection.class &&
                        entry.getKey() != ParentDataWidget.ParentDataCollection.class)
                    w = wrapWithProvide(entry, w);

        // ezt tartsuk szinkronvan Reuse.make-belivel
        w = new Provider<>(ResolutionRequest.ResolutionRequestCollection.class,
                new ResolutionRequest.ResolutionRequestCollection(this), w);
        w = new Provider<>(ParentDataWidget.ParentDataCollection.class,
                ParentDataWidget.ParentDataCollection.CLEAR, w, true);

        return w;
    }

    private static <T> Provider<T> wrapWithProvide(Map.Entry<Class<?>, Object> e, Widget w) {
        @SuppressWarnings("unchecked") Class<T> key = (Class<T>) e.getKey();
        return new Provider<>(key, key.cast(e.getValue()), w);
    }

    Widget secondaryWrapper() {
        return new Reuse(this);
    }


    static final class ResolutionRequestCollection {

        // for now, only one request at a time
        public final ResolutionRequest<?> request;

        public ResolutionRequestCollection(ResolutionRequest<?> request) {
            this.request = request;
        }

        // TODO equals?
    }

    static class Reuse extends Widget {

        final ResolutionRequest<?> req;

        public Reuse(ResolutionRequest<?> req) {
            this.req = req;
        }

        // TODO ha egy IV a primary helyen nincs, de secondary helyen van, viszont
        //      nem olvassa egyik helyen sem, majd secondary helyen megváltozik, majd
        //      primary helyen elkezdi olvasni, akkor ott a régi értéket fogja olvasni,
        //      és nem is lesz feltétlen refreshelve a secondary helyen

        WidgetInstantiation make(WidgetTree widgetTree, RefreshStack refreshStack, Map<Class<?>, Object> directIVs) {
            if (req.finisherWidget == null)
                throw new UnsupportedOperationException("finisher widget not specified");
            if (req.widgetState == null)
                throw new RuntimeException("RR wS null");

            Map<Class<?>, Object> ivsUntilFinisher = refreshStack.ivsUntil(req.finisherWidget);
            if (ivsUntilFinisher == null)
                throw new RuntimeException("Return value of " +
                        PeerCreationRequest.ResolutionResult.class.getSimpleName() + ".reuse() " +
                        "was put outside of a resolution finisher");
            ivsUntilFinisher.putAll(directIVs);

            if (req.refreshIDOfIVsFromSecondaryLocation == widgetTree.beganRefreshID)
                throw new RuntimeException("Return value of " +
                        PeerCreationRequest.ResolutionResult.class.getSimpleName() + ".reuse() " +
                        "was used multiple times in the widget tree");
            req.ivsFromSecondaryLocation = ivsUntilFinisher;
            req.refreshIDOfIVsFromSecondaryLocation = widgetTree.beganRefreshID;

            Map<Class<?>, Object> combined = new HashMap<>();

            combined.putAll(ivsUntilFinisher);
            combined.putAll(req.directIVsAtPrimaryLocation);

            // ezt tartsuk szinkronban ResolutionRequest.primaryWrapper-rel
            combined.put(ResolutionRequestCollection.class,
                    new ResolutionRequest.ResolutionRequestCollection(req));
            combined.put(ParentDataWidget.ParentDataCollection.class,
                    ParentDataWidget.ParentDataCollection.CLEAR);

            return new WidgetInstantiation(req.widgetState, combined, false);
        }

        @Override
        protected Widget build() {
            throw new RuntimeException("should not reach here");
        }
    }
}
