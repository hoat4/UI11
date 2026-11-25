package ui11.layout;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.SubstitutedWidget;
import ui11.Widget;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Gone extends SubstitutedWidget {

    private static final Gone INSTANCE = new Gone();

    private Gone() {
        if (INSTANCE != null)
            throw new Error();
    }

    public static Gone gone() {
        return INSTANCE;
    }

    public static @NonNull Widget goneIfNull(@Nullable Widget w) {
        return w == null ? gone() : w;
    }

    /**
     * @param array this won't be modified
     * @return an {@linkplain List#of(Object[]) immutable, random access, null-prohibiting} list
     */
    @SuppressWarnings("NullableProblems") // List.of-ra reklamál, de lecseréltük a nullokat
    public static @NonNull List<? extends @NonNull Widget> replaceNullsWithGone(@Nullable Widget @NonNull [] array) {
        for (int i = 0; i < array.length; i++) {
            Widget w = array[i];
            if (w == null) {
                array = array.clone(); // nem illik módosítani a bejövő tömböt, főleg ha varargs
                array[i] = gone();
                for (int j = i + 1; j < array.length; j++)
                    array[j] = Gone.goneIfNull(array[j]);
                break;
            }
        }
        return List.of(array);
    }

    /**
     * @param list this won't be modified
     * @return an {@linkplain List#of(Object[]) immutable, random access, null-prohibiting} list
     */
    public static @NonNull List<? extends @NonNull Widget> replaceNullsWithGone(
            @NonNull List<? extends @Nullable Widget> list) {
        // List.copyOf nem másolna feleslegesen, ha már java.util.ImmutableCollections$ListN a lista.
        // de azt nem lehet itt használni, mert lehet hogy tartalmaz nullt.

        Widget[] array = list.toArray(Widget[]::new);
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null)
                array[i] = gone();
        }
        return List.of(array);
    }

    @Override
    public String toString() {
        return "Gone";
    }
}
