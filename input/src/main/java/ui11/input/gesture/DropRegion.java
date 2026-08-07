package ui11.input.gesture;

import ui11.Key;
import ui11.SubstitutedWidget;
import ui11.Widget;
import ui11.geom.Location;

import org.jspecify.annotations.NonNull;
import java.awt.datatransfer.Transferable;
import java.util.Objects;

// TODO implementáljuk ezt néhány platformon
public final class DropRegion extends SubstitutedWidget {

    private final @NonNull DropListener listener;
    private final @NonNull Widget content;

    @Remember private Key contentKey;

    public DropRegion(@NonNull DropListener listener, @NonNull Widget content) {
        this.listener = Objects.requireNonNull(listener); // TODO eventListener proxy
        this.content = Objects.requireNonNull(content);
    }

    @Override
    protected void initState() {
        contentKey = Key.create();
    }

    @Override
    protected DropRegion forSubstitution() {
        return new DropRegion(listener, content.withKey(contentKey));
    }

    public @NonNull DropListener listener() {
        return listener;
    }

    public @NonNull Widget content() {
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
