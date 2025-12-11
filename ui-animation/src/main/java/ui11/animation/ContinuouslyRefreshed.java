package ui11.animation;

import ui11.Widget;

import java.util.function.Supplier;

/**
 * Minden animációs frameben újraszámolja a tartalmat.
 */
public class ContinuouslyRefreshed extends Widget {

    private final Supplier<Widget> contentSupplier;

    @Inject private Scheduler scheduler;

    public ContinuouslyRefreshed(Supplier<Widget> contentSupplier) {
        this.contentSupplier = contentSupplier;
    }

    @Override
    protected Widget build() {
        scheduler.requestAnimationFrame();
        return contentSupplier.get();
    }
}
