package ui11.input.gesture;

import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Location;

import javax.annotation.Nonnull;
import java.awt.datatransfer.Transferable;
import java.util.Objects;

// TODO implementáljuk ezt néhány platformon
public final class DropRegion extends SubstitutedWidget {

    @Nonnull private final DropListener listener;
    @Nonnull private final Widget content;

    public DropRegion(@Nonnull DropListener listener, @Nonnull Widget content) {
        this.listener = Objects.requireNonNull(listener); // TODO eventListener proxy
        this.content = Objects.requireNonNull(content);
    }

    @Nonnull
    public DropListener listener() {
        return listener;
    }

    @Nonnull
    public Widget content() {
        return content;
    }

    public interface DropListener {
        // return value nullability?
        DragOverCallback onDragOver(Transferable transferable, Location point);
    }

    public interface DragOverCallback {

        void drag(Location point);

        void exit();

        void enter();

        void cancel();

        void commit();
    }
}
