package ui11.provide;


import ui11.Widget;

// ~ Flutter InheritedWidget

// a value lehet null, lásd pl. GameContainer::lobby.

// valszeg azt jelenti a null érték, hogy a lookupkor itt álljunk meg a keresésben
// és ne menjünk feljebb.

public final class Provider<T> extends Widget {

    private final Class<T> type;
    private final T value;
    private final Widget content;

    /**
     * ideiglenesen publikus
     */
    @Deprecated
    public final boolean ignoreMergeableType;

    public Provider(Class<T> type, T value,
                    Widget content /* ez lehet null? */) {
        this.type = type;
        this.value = value;
        this.content = content;
        this.ignoreMergeableType = false;

        if (content != null && !Widget.class.isInstance(content)) // TeaVM-es kód bugjakor előjött egy ilyen
            throw new RuntimeException("not a widget (P): " + content);
    }

    /**
     * ideiglenesen publikus
     */
    @Deprecated
    public Provider(Class<T> type, T value, Widget content, boolean ignoreMergeableType) {
        this.type = type;
        this.value = value;
        this.content = content;
        this.ignoreMergeableType = ignoreMergeableType;
    }

    public Class<T> type() {
        return type;
    }

    // ha ennek a nevét vagy signaturejét megváltoztatjuk, írjuk át TeaVMWidgetAccessorban is
    public T value() {
        return value;
    }

    public Widget content() {
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
