package ui11;

import ui11.resolution.PeerCreationRequest;

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
}
