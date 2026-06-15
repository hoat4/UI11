package ui11;

import org.jspecify.annotations.NonNull;
import ui11.provide.Provider;
import ui11.resolution.PeerCreationRequest;
import ui11.resolution.PeerCreationRequestCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A widget that is fully concrete, e.g. doesn't build more widgets.
 * <p>
 * A widget can query the EndingWidget of a child using
 * {@link Widget#makePeer(Slot, Widget, PeerCreationRequest)}.
 */
public abstract class EndingWidget extends Widget {

    /**
     * Creates a new instance of EndingWidget.
     */
    protected EndingWidget() {
    }

    /**
     * Throws an exception, because EndingWidgets don't have state.
     */
    @Override
    protected final void initState() {
        throw new UnsupportedOperationException("An " + EndingWidget.class.getSimpleName() + " does not have state");
    }

    /**
     * Throws an exception, because EndingWidgets don't have state.
     */
    @Override
    protected final void onResume() {
        throw new UnsupportedOperationException("An " + EndingWidget.class.getSimpleName() + " does not have state");
    }

    /**
     * Throws an exception, because EndingWidgets don't have state.
     */
    @Override
    protected final Widget build() {
        throw new UnsupportedOperationException("An " + EndingWidget.class.getSimpleName() + " does not have state");
    }

    public static Widget combine(@NonNull Widget widget, @NonNull EndingWidget... endingWidgets) {
        Objects.requireNonNull(widget);
        List<@NonNull EndingWidget> endingWidgetList = List.of(endingWidgets);
        Set<Class<? extends EndingWidget>> endingWidgetTypes = new HashSet<>();
        for (EndingWidget e : endingWidgetList)
            endingWidgetTypes.add(e.getClass());
        widget = new RequestClearerWidget(Set.copyOf(endingWidgetTypes), widget);
        return new MultipleUpValues(widget, endingWidgetList);
    }

    static final class MultipleUpValues extends EndingWidget {

        final @NonNull Widget next;
        final @NonNull List<@NonNull EndingWidget> endingWidgets; // tömböt nem jól néz Widget.equals

        public MultipleUpValues(@NonNull Widget next, @NonNull List<@NonNull EndingWidget> endingWidgets) {
            this.next = next;
            this.endingWidgets = endingWidgets;
        }
    }

    private static class RequestClearerWidget extends Widget {

        private final Set<Class<? extends EndingWidget>> upValueTypes;
        private final Widget next;

        @Inject(required = false) private PeerCreationRequestCollection prevReqCollection;

        RequestClearerWidget(Set<Class<? extends EndingWidget>> upValueTypes, Widget next) {
            this.upValueTypes = upValueTypes;
            this.next = next;
        }

        @Override
        protected Widget build() {
            if (prevReqCollection == null || upValueTypes.stream().
                    noneMatch(prevReqCollection.request.peerType()::isAssignableFrom))
                return next;
            else
                return new Provider<>(PeerCreationRequestCollection.class,
                        new PeerCreationRequestCollection(DummyPeerCreationRequest.INSTANCE), next);
        }

        // csak azért kell, mert SubstitutedWidget arra számít, hogy mindenképpen van PeerCreationRequest
        static class DummyPeerCreationRequest extends PeerCreationRequest<DummyPeerCreationRequest.DummyPeer> {

            static final DummyPeerCreationRequest INSTANCE = new DummyPeerCreationRequest();

            private DummyPeerCreationRequest() {
                super(DummyPeer.class);
            }

            static class DummyPeer extends EndingWidget {}
        }
    }
}
