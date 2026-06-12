package ui11;

import java.util.Objects;

final class BuildContext {

    final Element element; // TODO valami tesztnek kell hogy package-privát legyen
    final boolean isStartOrRestart;
    final boolean isStart;

    BuildContext(Element element, boolean isStartOrRestart, boolean isStart) {
        this.element = element;
        this.isStartOrRestart = isStartOrRestart;
        this.isStart = isStart;
    }

    /**
     * Ennek az Elementnek a gyerekévé teszi a megadott Widgetből képezhető egy Elementet, ami a következő frissítésig
     * aktív fog maradni. Csak a frissítés közben hívható meg.
     *
     * @throws IllegalStateException ha nem REFRESHING_SELF állapotban van jelenleg az Element
     * @throws NullPointerException  ha a megadott widget {@code null}
     */
    // TODO ideiglenes instantiate? pl. MultiChildLayouthoz
    // TODO duplicate key detektálása, pl. DOMGridPeernél az egymásra rakható elemek kapcsán előjött
    //      upValues invalidálás bugot óráig tartott debugolni
    public WidgetInstantiation instantiate(Object key, Widget widget) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(widget, "widget");

        if (element.refreshState != this)
            throw new IllegalStateException();

        // 3. argumentum nem is kéne, de mindegy
        KeyWrapper implicitKey = new KeyWrapper(element, key, widget);

        return new ChainSegmentBuilder(element, widget, implicitKey).build();
    }
}
