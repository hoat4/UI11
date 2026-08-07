package ui11.input.gesture;


import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;

import org.jspecify.annotations.NonNull;

import java.util.Objects;

/**
 * Escape vagy böngésző back gomb vagy swipe. Nincs minden platformon támogatva, csak böngészők kb. 70%-án.
 */
public final class CloseRequestListener extends SubstitutedWidget {

    private final @NonNull Runnable onClose;
    private final @NonNull Widget content;

    @Remember private Key contentKey;

    public CloseRequestListener(@NonNull Runnable onClose, @NonNull Widget content) {
        this.onClose = listenerProxy(Objects.requireNonNull(onClose));
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected CloseRequestListener forSubstitution() {
        return new CloseRequestListener(onClose, content);
    }

    public @NonNull Runnable onClose() {
        return onClose;
    }

    public @NonNull Widget content() {
        return content;
    }

    // TODO valamit kéne csinálni hogy egy ki/be rakosgadása egy widgetnek egy CloseRequestListenerbe
    //      ne okozzon problémát, hanem jöjjön rá valahogy magától hogy annak az előző állapot folytatásának kéne
    //      lennie
    //      (meg nyilván ugyanez a többi listenereknél)

    @Override
    protected @NonNull Widget fallbackContent() {
        return content();
    }
}
