package ui11.provide;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import ui11.Widget;

import java.util.Objects;

// ~ Flutter InheritedWidget

// a value lehet null, lásd pl. GameContainer::lobby.

// valszeg azt jelenti a null érték, hogy a lookupkor itt álljunk meg a keresésben
// és ne menjünk feljebb.

public final class Provider<T> extends Widget {

    private final Class<T> type;
    private final T value;
    private final Widget content;

    public Provider(@NonNull Class<T> type, @Nullable T value, @NonNull Widget content) {
        Objects.requireNonNull(content);

        if (type.isPrimitive())
            // typeargban nem lehet rá hivatkozni
            throw new IllegalArgumentException("Type of inherited value must " +
                    "not be a primitive type: " + type.getName());

        this.type = type;
        this.value = value;
        this.content = content;

        if (!Widget.class.isInstance(content)) // TeaVM-es kód bugjakor előjött egy ilyen
            throw new RuntimeException("not a widget (P): " + content);
    }

    public Class<T> type() {
        return type;
    }

    // ha ennek a nevét vagy signaturejét megváltoztatjuk, írjuk át TeaVMWidgetAccessorban is
    public @Nullable T value() {
        return value;
    }

    public @NonNull Widget content() {
        return content;
    }

    @Override
    protected Widget build() {
        throw new RuntimeException("should not reach here (P b)");
    }

    @Override
    public String toString() {
        return "Provider (type=" + type.getName() + ", value=" + value + "): " + content;
    }

    public interface Mergeable<T> {
        // TODO dokumentáljuk, hogy mergölés hogy működik nullok esetén

        T mergeWith(T defaults);
    }
}
