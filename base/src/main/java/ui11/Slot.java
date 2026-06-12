package ui11;

import java.util.Map;
import java.util.Objects;

// LCW-t meg lehetne próbálni, hogy ne legyenek olyan távol a Slot definíciók a használatuktól.
// StackWalker használatán is gondolkodtam, de nem találtam értelmes felhasználást neki.

// 2025-11-08-ból megjegyzés keyekről:
//      withKey dobjon exceptiont ha duplicate
//      De ez nem olyan egyszerű. Mit tekintünk duplicate-nek?
//      - ha többször hívják meg ugyanazzal a kulccsal. De mi van, ha refreshSelf-en kívül van meghívva?
//      - többször van felhasználva a KeyWrapper. Ez se jó, mert ez lehet legális is:
//        pl. MultiChildLayoutImpl első alkalommal instantiateeli mérésre, második alkalommal meg berakja
//        Overlay/Transform-ba childnak.
//      Itt useComponentnél még egyszerű a helyzet, mert ezt csak refreshSelf közben lehet meghívni,
//      és nem is adjuk tovább a lookup eredményét.

// TODO le kéne írni, hogy a tipikus layout konténereknél nem kell ezt használni

/**
 * A Slot is an identifier for Widgets.
 * <p>
 * A new widget will only be used to update an existing element if its key is the same as the key of the current widget
 * associated with the element.
 * <p>
 * A Slot can be obtained using the {@link ui11.Widget.Inject @Inject} annotation in a Widget or by
 * {@linkplain MultiSlot#of(Object)}.
 *
 * @see MultiSlot
 */
public final class Slot {

    final Widget slotContainerWidget;
    final Object key;

    Slot(Widget w, Object key) {
        Objects.requireNonNull(w);
        Objects.requireNonNull(key);
        this.slotContainerWidget = w;
        this.key = key;
    }

    // név mi legyen? wrap? use? widget?
    public KeyWrapper use(Widget widget) {
        Objects.requireNonNull(widget);
        RSWStateHolder<?> stateHolder = slotContainerWidget.stateHolderOrNull();
        if (stateHolder == null || !stateHolder.isRefreshingSelfOrDescendants())
            // eredetileg csak REFRESH_SELF-ben lehetett,
            // de úgy nem lehetett egy descendant Elementből hívni (ld. pl. LobbyTabContent-nél chatet).
            // azt nem tudjuk ellenőrizni, hogy amikor this referenciát odaadta másnak, akkor
            // még ugyanaz volt-e a stateHolder.
            // TODO initStateből lehet hogy nem kéne tudni hívni
            throw new IllegalStateException(Slot.class.getSimpleName() +
                    ".use can only be called inside " + Widget.class.getSimpleName() + ".build(), " +
                    "initState(), onResume() or in descendants refresh. \n" +
                    "Slot container: " + stateHolder + "\n" +
                    "Refresh stack: \n" +
                    stateHolder.refreshStackToString(Map.of()));
        // TODO key duplicateek?
        return new KeyWrapper(stateHolder, key, widget);
    }

    public WidgetInstantiation instantiate(Widget widget) {
        Objects.requireNonNull(widget);

        RSWStateHolder<?> stateHolder = slotContainerWidget.stateHolderOrNull();
        if (stateHolder == null || stateHolder.refreshState == null)
            // TODO így initStateből is lehet hívni
            throw new IllegalStateException(Slot.class.getSimpleName() +
                    ".instantiate can only be called inside " + Widget.class.getSimpleName() + ".build()");

        @SuppressWarnings("unchecked") final WidgetAccessor<Widget> castedAccessor =
                (WidgetAccessor<Widget>) stateHolder.accessor;
        Widget decoratedWidget = castedAccessor.decorate(slotContainerWidget, widget, false);
        if (decoratedWidget == null)
            throw new RuntimeException("decorator returned null on " + slotContainerWidget + " for " + widget + " (slot: " + this + ")");

        return stateHolder.refreshState.instantiate(key, decoratedWidget);
    }

    // hogy meg lehessen adni mezőben
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Slot slot = (Slot) o;
        return slotContainerWidget.equals(slot.slotContainerWidget) && key.equals(slot.key);
    }

    @Override
    public int hashCode() {
        int result = slotContainerWidget.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }
}
